package cn.anoxia.chat.common.domain;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@ToString
public class ChatUser {
    private String id;
    private String username;
    private String nickname;
    private String password;
    private String avatarUrl;
    private String email;
    private String phone;
    private String status;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}