package cn.anoxia.chat.services;



import cn.anoxia.chat.common.domain.AttachmentDetail;
import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.ChatRoom;
import cn.anoxia.chat.common.domain.User;
import cn.anoxia.chat.common.enmu.MessageStatus;

import java.util.List;
import java.util.Map;

public interface IDataHandlerService {



    User loginUser(String name,String pwd,String ip);


    /**
     * 获取聊天室信息 根据用户ID
     *
     * @param userId 用户ID
     * @return 聊天室
     */
    ChatRoom getSingleChatRoom(String userId, String userId2, String assessmentId);

    /**
     * 获取聊天历史记录 默认是50条
     *
     * @param roomId 房间ID
     * @param start  开始
     * @param end    结束
     * @return 返回message
     */
    List<ChatMessage> getHistory(Long roomId, int start, int end);

    /**
     * 更新消息状态
     *
     * @param ids 要更新的消息ID
     * @param newStatus 要设置的状态
     * @return 返回执行结果
     */
    int upMessageStatus(List<String> ids, MessageStatus newStatus);

    /**
     * 持久化一批消息及其附件，并在成功后清除 Redis 缓存。
     *
     * @param roomMessageMap  Redis 中的消息队列
     * @param messages 待持久化的消息列表
     */
    void persist(Map<String, List<ChatMessage>> roomMessageMap, List<ChatMessage> messages);


    /**
     * 获取用户所有聊天室的最后一条消息
     * @param uid 用户ID
     * @return
     */
    List<ChatMessage> getUserLastMessage(String uid);


    /**
     * 持久化消息列表
     */
    void batchSaveMessage();

    /**
     * 获取附件详细
     * @param id 附件ID
     * @return 附件详细
     */
    AttachmentDetail getAttachmentDetail(String id);

}
