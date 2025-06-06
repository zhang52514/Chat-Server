package cn.anoxia.chat.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageStatus {
    sending, // 消息正在发送（客户端已发出但未收到服务端ACK）
    sent, // 消息已成功发送到服务端
    delivered, //消息已成功送达接收者
    read, //消息已被接收者阅读
    failed, // 消息发送失败（需重试）
}
