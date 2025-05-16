package cn.anoxia.chat.services.Impl;


import cn.anoxia.chat.common.domain.*;
import cn.anoxia.chat.common.enmu.ChatRoomType;
import cn.anoxia.chat.common.enmu.MessageStatus;
import cn.anoxia.chat.common.enmu.MessageType;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.mapper.*;
import cn.anoxia.chat.services.IDataHandlerService;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class IDataHandlerServiceImpl implements IDataHandlerService {

    private static final Logger log = LoggerFactory.getLogger(IDataHandlerServiceImpl.class);

    @Autowired
    private RedisServer redisServer;
    @Autowired
    private ChatRoomMapper chatRoomMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private ChatRoomUserMapper chatRoomUserMapper;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User loginUser(String name, String pwd,String ip) {
        User user = userMapper.selectByUsernameAndPassword(name, pwd);
        if (user != null) {
            user.setLastLoginTime(new Date());
            user.setLastLoginIp(ip);
            userMapper.update(user);
        }
        return user;
    }

    @Override
    @Transactional
    public ChatRoom getSingleChatRoom(String userId, String userId2, String assessmentId) {
        Long privateRoomId = chatRoomUserMapper.getPrivateRoomId(userId, userId2, assessmentId);
        if (privateRoomId == null) {
            ChatRoom room = new ChatRoom();
            room.setAssessmentId(assessmentId);
            room.setRoomType(ChatRoomType.SINGLE);
            room.setCreatedTime(new Date());
            room.setRoomStatus("0");
            room.setDescription(userId + "Created ChatRoom Success");
            int rows = chatRoomMapper.insertRoom(room);
            if (rows > 0) {
                ChatRoomUser roomUser = new ChatRoomUser();
                roomUser.setRoomId(room.getRoomId());
                roomUser.setUserId(userId);
                roomUser.setJoinTime(new Date());
                ChatRoomUser roomUser2 = new ChatRoomUser();
                roomUser2.setRoomId(room.getRoomId());
                roomUser2.setUserId(userId2);
                roomUser2.setJoinTime(new Date());
                int insert = chatRoomUserMapper.insert(roomUser);
                int insert1 = chatRoomUserMapper.insert(roomUser2);
                if (insert > 0 && insert1 > 0) {
                    return room;
                }
                return null;
            }
            return null;
        }
        return chatRoomMapper.selectById(privateRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getHistory(Long roomId, int start, int end) {
        int count = end - start;
        List<ChatMessage> messages = redisServer.getMessages(roomId, start, end);
        log.info("获取room-{}历史记录大小：{}", roomId, messages.size());
        List<ChatMessage> result = new ArrayList<>();

        // 初始化消息 ID 列表
        List<String> messageIds = new ArrayList<>();
        if (!messages.isEmpty()) {
            messageIds.addAll(messages.stream()
                    .map(ChatMessage::getMessageId)
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (messages.size() < count) {
            int remainingCount = count - messages.size();
            int dbStart = Math.max(0, start - remainingCount);

            List<ChatMessage> dbMessages = chatMessageMapper.selectMessageByHistory(roomId, dbStart, remainingCount);

            if (!dbMessages.isEmpty()) {
                List<String> dbMessageIds = dbMessages.stream()
                        .map(ChatMessage::getMessageId)
                        .filter(Objects::nonNull)
                        .toList();
                messageIds.addAll(dbMessageIds);

                // 附件只在 messageIds 非空时查询
                if (!messageIds.isEmpty()) {
                    List<Attachment> attachmentList = attachmentMapper.selectByMessageIds(messageIds);

                    Map<String, List<Attachment>> messageAttachmentsMap = new HashMap<>();
                    for (Attachment attachment : attachmentList) {
                        messageAttachmentsMap
                                .computeIfAbsent(attachment.getMessageId(), k -> new ArrayList<>())
                                .add(attachment);
                    }

                    for (ChatMessage message : dbMessages) {
                        message.setAttachment(messageAttachmentsMap.getOrDefault(message.getMessageId(), List.of()));
                    }
                }

                result.addAll(dbMessages); // db 消息先放入结果
            }
        }

        result.addAll(messages); // redis 消息补到结果后面
        // 最终统一按时间排序
        result.sort(Comparator.comparing(ChatMessage::getTimestamp));
        return result;
    }


    @Override
    public int upMessageStatus(List<String> ids, MessageStatus newStatus) {
        return chatMessageMapper.batchUpdateMessageStatus(ids, newStatus);
    }


    /**
     * 持久化一批消息及其附件，并在成功后清除 Redis 缓存。
     *
     * @param roomMessageMap Redis 中的消息队列
     * @param messages       待持久化的消息列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void persist(Map<String, List<ChatMessage>> roomMessageMap, List<ChatMessage> messages) {
        // 1. 批量插入消息
        int inserted = chatMessageMapper.batchInsertMessages(messages);
        log.info("共计批量插入消息条数：{}", inserted);

        if (inserted <= 0) {
            log.warn("消息插入失败，跳过后续处理");
            return;
        }

        // 2. 提取并插入附件
        List<Attachment> attachments = extractAttachments(messages);
        if (!attachments.isEmpty()) {
            int attInserted = attachmentMapper.batchInsertMessages(attachments);
            log.info("批量插入附件条数：{}", attInserted);

            if (attInserted <= 0) {
                // 如果附件插入失败，则抛出异常，触发事务回滚
                throw new RuntimeException("附件插入失败");
            }
        }

        // 3. 全部成功后，删除 Redis 缓存
        // 删除 Redis 中的所有房间消息
        for (String roomKey : roomMessageMap.keySet()) {
            redisServer.getRedisTemplate().delete(roomKey);
            log.info("已删除 Redis 中的 roomKey={}", roomKey);
        }
        log.info("所有消息和附件已持久化成功");
    }

    @Override
    public List<ChatMessage> getUserLastMessage(String uid) {

        //获取用户聊天室
        List<ChatRoomUser> chatRoomUsers = chatRoomUserMapper.selectUserRoom(uid);
        if (chatRoomUsers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roomIds = chatRoomUsers.stream().map(ChatRoomUser::getRoomId).toList();
        //roomId 查询缓存  缓存不存在  查询DB
        List<ChatMessage> lastMessagesList = redisServer.getLastMessagesList(roomIds);

        // 获取 Redis 中没有的 roomIds（即未命中的聊天室 ID）
//        List<Long> difference = roomIds.stream()
//                .filter(roomId -> lastMessagesList.stream()
//                        .noneMatch(message -> message.getRoom().getRoomId().equals(roomId)))
//                .toList();
//
//        if (!difference.isEmpty()) {
//            List<ChatMessage> dbMessages = chatMessageMapper.selectLastMessagesByRoomIds(difference);
//            lastMessagesList.addAll(dbMessages);
//        }

        return lastMessagesList;
    }

    @Override
    public void batchSaveMessage() {
        log.info("定时任务：开始扫描 Redis 中待持久化消息");
        Set<String> roomKeys = redisServer.scanKeys("normal:messages:*");
        if (roomKeys.isEmpty()) {
            log.info("未发现待持久化的消息队列");
            return;
        }
        List<ChatMessage> allMessages = new ArrayList<>();
        Map<String, List<ChatMessage>> roomMessageMap = new HashMap<>();

        for (String roomKey : roomKeys) {
            List<ChatMessage> messages = extractMessagesFromRedis(roomKey);
            if (!messages.isEmpty()) {
                allMessages.addAll(messages);
                roomMessageMap.put(roomKey, messages);
            }
        }

        if (allMessages.isEmpty()) {
            log.info("所有房间均无消息，跳过持久化");
            return;
        }

        try {
            this.persist(roomMessageMap, allMessages);
        } catch (Exception e) {
            log.error("批量持久化失败", e);
        }
    }

    @Override
    public AttachmentDetail getAttachmentDetail(String id) {
        Set<String> roomKeys = redisServer.scanKeys("normal:messages:*");
        if (!roomKeys.isEmpty()) {
            List<ChatMessage> allMessages = new ArrayList<>();
            for (String roomKey : roomKeys) {
                List<ChatMessage> messages = extractMessagesFromRedis(roomKey);
                if (!messages.isEmpty()) {
                    allMessages.addAll(messages);
                }
            }

            if (allMessages.isEmpty()) {
                return attachmentMapper.selectAttachmentDetailByMessageId(id);
            }

            for (ChatMessage msg : allMessages) {
                if (msg.getType() == MessageType.FILE && msg.getAttachment() != null) {
                    for (Attachment att : msg.getAttachment()) {
                        if (att.getId().equals(id)) {
                            return getAttachmentDetail(id, msg, att);
                        }
                    }
                }
            }
        }
        return attachmentMapper.selectAttachmentDetailByMessageId(id);
    }

    private AttachmentDetail getAttachmentDetail(String id, ChatMessage msg, Attachment att) {
        AttachmentDetail attDetail = new AttachmentDetail();
        attDetail.setId(id);
        attDetail.setMessageId(msg.getMessageId());
//        attDetail.setRoomId(msg.getRoom().getRoomId());
        attDetail.setSenderId(msg.getSenderId());
        attDetail.setReceiverId(msg.getReceiverId());
        attDetail.setStatus(msg.getStatus());
        attDetail.setTimestamp(msg.getTimestamp());
        attDetail.setUrl(att.getUrl());
        attDetail.setName(att.getName());
        attDetail.setType(att.getType());
        attDetail.setSize(att.getSize());
        return attDetail;
    }

    /**
     * 从 Redis Hash 中读取并反序列化所有消息。
     */
    private List<ChatMessage> extractMessagesFromRedis(String roomKey) {
        Map<Object, Object> entries = redisServer.getRedisTemplate().opsForHash().entries(roomKey);
        List<ChatMessage> messages = new ArrayList<>(entries.size());
        for (Object value : entries.values()) {
            ChatMessage msg = JSON.parseObject(value.toString(), ChatMessage.class);
            messages.add(msg);
        }
        return messages;
    }

    /**
     * 从消息列表中抽取所有附件对象。
     */
    private List<Attachment> extractAttachments(List<ChatMessage> messages) {
        List<Attachment> attachments = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (msg.getAttachment() != null && !msg.getAttachment().isEmpty()) {
                attachments.addAll(msg.getAttachment());
            }
        }
        return attachments;
    }
}
