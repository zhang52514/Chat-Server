package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.enmu.MessageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AttachmentDetail {
    private String id;
    private String messageId;
    private Long roomId;
    private String senderId;
    private String receiverId;
    private MessageStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    private String url;
    private String name;
    private String type;
    private Long size;
}
