package cn.anoxia.chat.core.handler.auth;

import cn.anoxia.chat.common.domain.AuthMessage;
import cn.anoxia.chat.common.domain.User;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.common.utils.ErrorCode;
import cn.anoxia.chat.common.utils.JwtUtils;
import cn.anoxia.chat.core.handler.base.OnMessage;
import cn.anoxia.chat.core.manager.ChannelManager;
import cn.anoxia.chat.core.manager.SessionManager;
import cn.anoxia.chat.core.manager.TokenManager;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天连接处理器
 */
@Component
public class AuthHandler extends OnMessage {
    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);


    public AuthHandler(RedisServer redisServer) {
        super(redisServer);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService dataHandlerService) {
        try {
            if (!(msg instanceof AuthMessage authMsg)) {
                ctx.writeAndFlush(ResponseDto.failure(ErrorCode.BAD_REQUEST, "无效的请求类型"));
                return;
            }

            String ip = getRemoteIp(ctx);
            User user = dataHandlerService.loginUser(authMsg.getUserName(), authMsg.getUserPwd(), ip);

            if (user == null) {
                sendFailure(ctx, ErrorCode.AUTH_FAILED, "用户名或者密码错误");
                return;
            }

            if (user.getStatus() == 0) {
                sendFailure(ctx, ErrorCode.AUTH_FAILED, "账户被禁用");
                return;
            }

            // 绑定 session 和通道
            String sessionId = ctx.channel().id().asLongText();
            SessionManager.add(sessionId, user.getId().toString());
            ChannelManager.add(user.getId().toString(), ctx);

            // 生成并绑定 token
            String token = JwtUtils.generateToken(authMsg.getUserName());
            TokenManager.add(user.getId().toString(), token);

            log.info("用户登录成功 => ID: {}, IP: {}", user.getId(), ip);

            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("cmd", "auth");
            responsePayload.put("user", user);
            responsePayload.put("key", token);

            ctx.writeAndFlush(ResponseDto.success(responsePayload));

        } catch (Exception e) {
            log.error("用户登录异常", e);
            sendFailure(ctx, ErrorCode.SERVER_ERROR, "服务器内部错误");
        }
    }
}
