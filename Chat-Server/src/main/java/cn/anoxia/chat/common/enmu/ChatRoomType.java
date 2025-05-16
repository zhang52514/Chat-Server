package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatRoomType {
    CONNECT,//CONNECT 连接认证信息
    SINGLE,//SINGLE   私聊消息
    GROUP,//GROUP     群组消息
    SYSTEM,//SYSTEM   系统推送消息
    READ_RECEIPT;//READ_RECEIPT   已读回执
}
