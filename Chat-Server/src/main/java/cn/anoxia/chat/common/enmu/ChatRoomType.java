package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatRoomType {
    single, //SINGLE   私聊
    group, //GROUP     群组
    ai, //   AI
}
