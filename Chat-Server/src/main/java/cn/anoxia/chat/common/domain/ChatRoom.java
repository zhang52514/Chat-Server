package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.enmu.ChatRoomType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class ChatRoom {
    private Long roomId;
    private String assessmentId;
    private String roomName;
    private String roomIcon;
    private String roomStatus; //房间状态 可根据状态扩展  比如封禁   目前 "0" -> 正常
    private ChatRoomType roomType;  // 数据库存储的是 int 类型
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;
    private String description;
}
