package cn.anoxia.chat.mapper;

import cn.anoxia.chat.common.domain.ChatUser;

import java.util.List;

public interface ChatUserMapper {

    ChatUser selectById(Long id);

    ChatUser selectByUsername(String username);
    ChatUser selectByUsernameAndPassword(String username,String password);

    List<ChatUser> selectAll();

    int insert(ChatUser user);

    int update(ChatUser user);

    int deleteById(Long id);
}
