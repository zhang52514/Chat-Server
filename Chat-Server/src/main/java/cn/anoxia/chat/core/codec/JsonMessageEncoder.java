package cn.anoxia.chat.core.codec;

import cn.anoxia.chat.common.domain.dto.ResponseDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonMessageEncoder extends MessageToMessageEncoder<ResponseDto> {
    private static final Logger logger = LoggerFactory.getLogger(JsonMessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ResponseDto msg, List<Object> out) {

//        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
//        filter.getExcludes().add("ipAddress");
//        filter.getExcludes().add("token");

//        String json = JSON.toJSONString(msg, filter);
        String json = JSON.toJSONString(msg, JSONWriter.Feature.WriteNulls,JSONWriter.Feature.WriteEnumsUsingName);
        out.add(new TextWebSocketFrame(json));
    }
}
