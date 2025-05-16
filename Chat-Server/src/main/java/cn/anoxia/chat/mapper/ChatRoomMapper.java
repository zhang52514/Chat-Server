package cn.anoxia.chat.mapper;


import cn.anoxia.chat.common.domain.ChatRoom;

public interface ChatRoomMapper {

    int insertRoom(ChatRoom chatRoom);

    ChatRoom selectById(Long roomId);

}
