package cn.anoxia.chat.mapper;


import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.enmu.MessageStatus;

import java.util.List;

public interface ChatMessageMapper {
    /**
     * 批量插入聊天消息
     *
     * @param messages 聊天消息列表
     * @return 插入条数
     */
    int batchInsertMessages(List<ChatMessage> messages);

    /**
     * 查询历史聊天消息
     *
     * @param roomId 聊天室ID
     * @param start  起始索引
     * @param end    结束索引
     * @return 聊天消息列表
     */
    List<ChatMessage> selectMessageByHistory(String roomId, int start, int end);

    /**
     * 批量更新消息状态
     *
     * @param messageIds messageId
     * @param newStatus  消息状态
     * @return 更新条数
     */
    int batchUpdateMessageStatus(List<String> messageIds, MessageStatus newStatus);

    List<ChatMessage> selectLastMessagesByRoomIds(List<Long> roomIds);

}
