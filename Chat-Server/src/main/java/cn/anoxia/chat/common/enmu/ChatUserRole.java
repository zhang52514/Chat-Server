package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatUserRole {
    owner, // 群主
    admin, // 管理员
    member, // 普通成员
}
