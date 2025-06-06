package cn.anoxia.chat.mapper;


import cn.anoxia.chat.common.domain.ChatRoom;

import java.util.List;

public interface ChatRoomMapper {

    int insertRoom(ChatRoom chatRoom);

    ChatRoom selectById(Long roomId);
    List<ChatRoom> selectInId(List<String> roomIds);

}
