package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.domain.dto.RequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证Message
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuthMessage extends RequestDto {
    private String userName;
    private String userPwd;
}
