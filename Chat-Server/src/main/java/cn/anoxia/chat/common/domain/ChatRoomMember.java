package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.enmu.ChatUserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ChatRoomMember {
    private String roomId;
    private String userId;
    private ChatUserRole role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinedAt;
}
