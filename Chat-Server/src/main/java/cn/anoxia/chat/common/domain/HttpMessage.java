package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.domain.dto.RequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class HttpMessage extends RequestDto {
    //请求路径
    private String path;

    //请求参数
    private Map<String, Object> param;

    //token
    private String token;
}
