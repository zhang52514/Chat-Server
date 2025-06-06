package cn.anoxia.chat.mapper;


import cn.anoxia.chat.common.domain.ChatRoomMember;

import java.util.List;

public interface ChatRoomMemberMapper {

    int insert(ChatRoomMember chatRoomUser);

    /**
     * 获取两个用户的私聊房间ID
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 房间ID
     */
    Long getPrivateRoomId(String userId1, String userId2,String assessment);


    /**
     * 获取用户加入的聊天室列表
     * @param uid 用户ID
     * @return
     */
    List<ChatRoomMember> selectUserRoom(String uid);

    List<ChatRoomMember> selectAllMembersInRoomIds(List<String> roomIds);
}
