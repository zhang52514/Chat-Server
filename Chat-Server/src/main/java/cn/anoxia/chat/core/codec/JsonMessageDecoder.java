package cn.anoxia.chat.core.codec;

import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.common.domain.factory.MessageFactory;
import cn.anoxia.chat.common.utils.ErrorCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.InetSocketAddress;
import java.util.List;

public class JsonMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg, List<Object> out) throws Exception {
        String text = msg.text();
        InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = socket.getAddress().getHostName();
        RequestDto message = MessageFactory.parse(text, ip);

        if (!MessageFactory.verify(message)) {
            ctx.writeAndFlush(ResponseDto.failure(ErrorCode.AUTH_FAILED, "无权访问"));
            return;
        }
        out.add(message);
    }
}
