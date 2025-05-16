package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.enmu.MessageStatus;
import cn.anoxia.chat.common.enmu.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class ChatMessage  extends RequestDto {
    private String messageId;    // 消息ID
    private String senderId;    // 发送者ID
    private String receiverId;  // 接收者ID
    private String content;     // 消息内容
    private MessageStatus status;   // 消息状态
    private MessageType type;   // 消息类型
    private List<Attachment> attachment;   //附件地址
    private String roomId;;  //聊天房间
    private List<String> read;  // 存储已读消息的接收者ID列表
    private Map<String, Object> metadata; //附加信息
}
