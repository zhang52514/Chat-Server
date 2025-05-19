package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatRoomType {
    SINGLE,//SINGLE   私聊
    GROUP,//GROUP     群组
    AI,//SYSTEM   AI
}
