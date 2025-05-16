package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.core.handler.auth.AuthRequired;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class HttpMessage extends RequestDto implements AuthRequired {
    //请求路径
    private String path;

    //请求参数
    private Map<String, Object> param;

    //token
    private String token;

    @Override
    public String getToken() {
        return token;
    }
}
