package cn.anoxia.chat.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Attachment {
    private String id;
    private String messageId;
    private String url;
    private String name;
    private String type; // image/png, video/mp4, etc.
    private Long size;   // 字节数
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
}
