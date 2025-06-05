package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatRoomStatus {
    normal, // 正常
    muted, // 全员禁言
    blocked, // 封禁（例如被举报）
    deleted, // 解散
}
