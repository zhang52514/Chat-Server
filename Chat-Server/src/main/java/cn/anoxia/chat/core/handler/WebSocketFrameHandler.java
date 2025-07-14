package cn.anoxia.chat.core.handler;


import cn.anoxia.chat.common.domain.AuthMessage;
import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.HttpMessage;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.common.enmu.MessageType;
import cn.anoxia.chat.core.handler.auth.AuthHandler;
import cn.anoxia.chat.core.handler.chat.CallHandler;
import cn.anoxia.chat.core.handler.chat.ReadReceiptHandler;
import cn.anoxia.chat.core.handler.chat.SingleChatHandler;
import cn.anoxia.chat.core.handler.http.HttpHandler;
import cn.anoxia.chat.core.manager.ChannelManager;
import cn.anoxia.chat.core.manager.SessionManager;
import cn.anoxia.chat.core.manager.TokenManager;
import cn.anoxia.chat.services.IDataHandlerService;
import io.micrometer.common.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.InetSocketAddress;

/**
 * 消息处理器
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<RequestDto> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    private final IDataHandlerService dataHandlerService;
    private final AuthHandler authHandler;
    private final SingleChatHandler singleChatHandler;
    private final ReadReceiptHandler readReceiptHandler;
    private final CallHandler callHandler;
    private final HttpHandler httpHandler;


    public WebSocketFrameHandler(ConfigurableApplicationContext applicationContext) {
        dataHandlerService = applicationContext.getBean(IDataHandlerService.class);
        httpHandler = applicationContext.getBean(HttpHandler.class);
        authHandler = applicationContext.getBean(AuthHandler.class);
        singleChatHandler = applicationContext.getBean(SingleChatHandler.class);
        callHandler = applicationContext.getBean(CallHandler.class);
        readReceiptHandler = applicationContext.getBean(ReadReceiptHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestDto msg) {
        //认证消息
        if (msg instanceof AuthMessage authMsg) {
            this.authHandler.execute(ctx, authMsg, dataHandlerService);
            return;
        }

        //聊天消息
        if (msg instanceof ChatMessage chatMsg) {
            if (chatMsg.getType() == MessageType.signal ||
                    chatMsg.getType() == MessageType.videoCall ||
                    chatMsg.getType() == MessageType.videoHangup ||
                    chatMsg.getType() == MessageType.videoAnswer ||
                    chatMsg.getType() == MessageType.videoReject) {

                this.callHandler.execute(ctx, chatMsg, this.dataHandlerService); // 新增通话处理器
                return;
            }
            this.singleChatHandler.execute(ctx, chatMsg, this.dataHandlerService);
            return;
        }

        //http消息
        if (msg instanceof HttpMessage httpMsg) {
            this.httpHandler.execute(ctx, httpMsg, dataHandlerService);
            return;
        }

        ctx.writeAndFlush(ResponseDto.failure("500", "Unknown message type"));
    }

    //当web客户端连接后， 触发方法
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
//        String remoteAddress = socket.getAddress().getHostName();
//        logger.info("socket->新增连接，ip：{}", remoteAddress);
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
        String remoteAddress = socket.getAddress().getHostName();
//        logger.info("socket->断开连接，ip：{}", remoteAddress);
        removeConnect(ctx);
        logger.info("断开连接->{}:当前连接数：{}, 用户连接：{}", remoteAddress, SessionManager.size(), ChannelManager.size());
        super.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(ResponseDto.failure("500", "服务器内部错误，关闭连接"));
        removeConnect(ctx);
        logger.error("发送异常，即将断开连接", cause);
        ctx.close();
    }

    /**
     * 移除链接
     *
     * @param ctx
     */
    private void removeConnect(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.channel() == null) {
            return;
        }
        String sessionId = ctx.channel().id().asLongText();
        String key = SessionManager.get(sessionId);
        if (StringUtils.isBlank(key)) {
            return;
        }

        // 移除用户连接，并清除会话信息
        try {
            ChannelManager.remove(key, ctx);  // 从 ChannelManager 中移除连接
            SessionManager.remove(sessionId);  // 从 SessionManager 中移除会话
            TokenManager.remove(key); //移除用户Token
        } catch (Exception e) {
            logger.error("删除用户连接出现异常：{}", sessionId, e);
        }
    }

}
