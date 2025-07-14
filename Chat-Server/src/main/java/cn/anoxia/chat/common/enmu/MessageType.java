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
    videoCall, // 视频通话请求
    videoAnswer, // 视频通话接听
    videoReject, // 视频通话拒绝
    videoHangup, // 视频挂断
    signal, // SDP、ICE等信令
}
