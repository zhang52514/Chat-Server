package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    text,
    file,
    image,
    audio,
    video,
    quill, // 富文本（富内容）
    emoji,
    system, // 系统通知
    aiReply, // AI回复（特化处理）
}
