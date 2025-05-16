package cn.anoxia.chat.mapper;

import cn.anoxia.chat.common.domain.User;

import java.util.List;

public interface UserMapper {

    User selectById(Long id);

    User selectByUsername(String username);
    User selectByUsernameAndPassword(String username,String password);

    List<User> selectAll();

    int insert(User user);

    int update(User user);

    int deleteById(Long id);
}
