package cn.anoxia.chat.common.domain;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class User {
    private Long id;
    private String username;
    private String nickname;
    private String password;
    private String avatarUrl;
    private String email;
    private String phone;
    private Integer status;
    private String role;
    private String lastLoginIp;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
}