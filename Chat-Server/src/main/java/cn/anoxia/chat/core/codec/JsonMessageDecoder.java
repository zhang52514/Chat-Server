package cn.anoxia.chat.core.codec;

import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.common.domain.factory.MessageFactory;
import cn.anoxia.chat.common.utils.ErrorCode;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.InetSocketAddress;
import java.util.List;

public class JsonMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg, List<Object> out) {
        try {
            String text = msg.text();
            InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
            String ip = socket.getAddress().getHostName();
            RequestDto message = MessageFactory.parse(text, ip);

            if (!MessageFactory.verify(message)) {
                String resJson = JSON.toJSONString(ResponseDto.failure(ErrorCode.AUTH_FAILED, "无权访问"));
                ctx.writeAndFlush(new TextWebSocketFrame(resJson));
                return;
            }
            out.add(message);
        } catch (Exception e) {
            String resJson = JSON.toJSONString(ResponseDto.failure(ErrorCode.BAD_REQUEST, "非法请求"));
            ctx.writeAndFlush(new TextWebSocketFrame(resJson));
        }
    }
}
