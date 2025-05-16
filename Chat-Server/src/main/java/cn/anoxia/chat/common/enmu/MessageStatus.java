package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageStatus {
    SENDING,      // 消息正在发送（客户端已发出但未收到服务端ACK）
    SENT,         // 消息已成功发送到服务端
    DELIVERED,         //消息已成功送达接收者
    READ,      //消息已被接收者阅读
    FAILED;     // 消息发送失败（需重试）
}
