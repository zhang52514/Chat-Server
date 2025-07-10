package cn.anoxia.chat.common.domain;

import cn.anoxia.chat.common.enmu.ChatRoomStatus;
import cn.anoxia.chat.common.enmu.ChatRoomType;
import cn.anoxia.chat.common.enmu.ChatUserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class ChatRoom {
    private String roomId;
    private String roomName;
    private String roomAvatar;
    private String roomDescription;
    private ChatRoomStatus roomStatus;
    private ChatRoomType roomType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    ///数据组装 成员 + 权限
    private List<String> memberIds;
    private Map<String, ChatUserRole> memberRoles;
}
