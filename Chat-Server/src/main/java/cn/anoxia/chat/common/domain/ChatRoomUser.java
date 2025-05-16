package cn.anoxia.chat.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ChatRoomUser {
    private Long id;
    private Long roomId;
    private String userId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
