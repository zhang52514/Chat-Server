package cn.anoxia.chat.services.Impl;


import cn.anoxia.chat.common.domain.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private ChatRoomMemberMapper chatRoomUserMapper;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private ChatUserMapper chatuserMapper;


    @Override
    @Transactional
    public ChatRoom getSingleChatRoom(String userId, String userId2, String assessmentId) {
//        Long privateRoomId = chatRoomUserMapper.getPrivateRoomId(userId, userId2, assessmentId);
//        if (privateRoomId == null) {
//            ChatRoom room = new ChatRoom();
//            room.setRoomType(ChatRoomType.single);
//            room.setCreatedTime(new Date());
//            room.setRoomStatus(ChatRoomStatus.normal);
//            room.setDescription(userId + "Created ChatRoom Success");
//            int rows = chatRoomMapper.insertRoom(room);
//            if (rows > 0) {
//                ChatRoomUser roomUser = new ChatRoomUser();
//                roomUser.setRoomId(room.getRoomId());
//                roomUser.setUserId(userId);
//                roomUser.setJoinTime(new Date());
//                ChatRoomUser roomUser2 = new ChatRoomUser();
//                roomUser2.setRoomId(room.getRoomId());
//                roomUser2.setUserId(userId2);
//                roomUser2.setJoinTime(new Date());
//                int insert = chatRoomUserMapper.insert(roomUser);
//                int insert1 = chatRoomUserMapper.insert(roomUser2);
//                if (insert > 0 && insert1 > 0) {
//                    return room;
//                }
//                return null;
//            }
//            return null;
//        }
        return chatRoomMapper.selectById(12L);
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

//        //获取用户聊天室
//        List<ChatRoomMember> chatRoomUsers = chatRoomUserMapper.selectUserRoom(uid);
//        if (chatRoomUsers.isEmpty()) {
//            return Collections.emptyList();
//        }
//        List<String> roomIds = chatRoomUsers.stream().map(ChatRoomMember::getRoomId).toList();
//        //roomId 查询缓存  缓存不存在  查询DB
//        List<ChatMessage> lastMessagesList = redisServer.getLastMessagesList(roomIds);

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

        return null;
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
                if (msg.getType() == MessageType.file && msg.getAttachment() != null) {
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


    //    ------------------------- =========================== V2
    @Override
    public ChatUser loginUser(String name, String pwd, String ip) {
        ChatUser user = chatuserMapper.selectByUsernameAndPassword(name, pwd);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            chatuserMapper.update(user);
        }
        return user;
    }

    @Override
    public List<ChatRoom> getRooms(String id) {
        // Step 1: 获取该用户参与的房间
        List<ChatRoomMember> joinedRooms = chatRoomUserMapper.selectUserRoom(id);
        if (joinedRooms.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> roomIds = joinedRooms.stream()
                .map(ChatRoomMember::getRoomId)
                .distinct()
                .collect(Collectors.toList());

        // Step 2: 查询这些房间的基本信息
        List<ChatRoom> chatRooms = chatRoomMapper.selectInId(roomIds);
        // Step 3: 查询这些房间下的所有成员
        List<ChatRoomMember> allMembers = chatRoomUserMapper.selectAllMembersInRoomIds(roomIds);

        // Step 4: 根据 roomId 组织成员数据
        Map<String, List<ChatRoomMember>> roomIdToMembers = allMembers.stream()
                .collect(Collectors.groupingBy(ChatRoomMember::getRoomId));

        for (ChatRoom room : chatRooms) {
            List<ChatRoomMember> members = roomIdToMembers.get(room.getRoomId());

            if (members != null) {
                room.setMemberIds(
                        members.stream()
                                .map(ChatRoomMember::getUserId)
                                .collect(Collectors.toList())
                );

                room.setMemberRoles(
                        members.stream().collect(Collectors.toMap(
                                ChatRoomMember::getUserId,
                                ChatRoomMember::getRole
                        ))
                );
            }
        }

        return chatRooms;
    }

    @Override
    public List<ChatUser> getUsers(String id) {
        return chatuserMapper.selectUser(id);
    }


    @Override
    @Transactional(readOnly = true)
    public Map<String, List<ChatMessage>> getAllRoomMessagesWithDbAndAttachments(
            String uid, int limitPerRoom) {

        Map<String, List<ChatMessage>> result = new HashMap<>();

        // 1. 获取用户所有房间
        List<ChatRoomMember> joinedRooms = chatRoomUserMapper.selectUserRoom(uid);
        if (joinedRooms.isEmpty()) {
            return result;
        }

        // 2. 构造房间 ID 列表
        List<String> roomIds = joinedRooms.stream()
                .map(ChatRoomMember::getRoomId)
                .toList();

        // 3. 从 Redis 拉取实时消息
        Map<String, List<ChatMessage>> redisMap = new HashMap<>();
        for (String roomId : roomIds) {
            String redisKey = "normal:messages:" + roomId;
            List<Object> raw = redisServer.getRedisTemplate()
                    .opsForHash().values(redisKey);
            if (raw != null && !raw.isEmpty()) {
                List<ChatMessage> msgs = raw.stream()
                        .map(o -> JSON.parseObject((String) o, ChatMessage.class))
                        .filter(m -> m.getMessageId() != null)
                        .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                        .toList();
                redisMap.put(roomId, msgs);
            }
        }

        // 4. 一次性从 DB 拉取：每个房间最新 limitPerRoom 条
        List<ChatMessage> dbAll = chatMessageMapper
                .selectMessageByHistory(roomIds, 0, limitPerRoom);

        // 5. 按房间分组
        Map<String, List<ChatMessage>> dbMap = dbAll.stream()
                .collect(Collectors.groupingBy(ChatMessage::getRoomId));

        // 6. 合并 Redis + DB、去重、排序、截取
        for (String roomId : roomIds) {
            List<ChatMessage> redisMsgs = redisMap.getOrDefault(roomId, List.of());
            // 还需要从 DB 拿多少条
            int need = limitPerRoom - redisMsgs.size();
            // 从 DB 分组里拿到该房间全部历史（已升序）
            List<ChatMessage> dbMsgs = dbMap.getOrDefault(roomId, List.of()).stream()
                    .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                    .toList();

            // —— 边界校验与截取 —— //
            if (need > 0 && dbMsgs.size() > need) {
                int total = dbMsgs.size();
                int fromIndex = Math.max(0, total - need);
                // 确保 fromIndex <= total
                if (fromIndex <= total) {
                    dbMsgs = new ArrayList<>(dbMsgs.subList(fromIndex, total));
                }
            }
            // 如果 need <= 0，则说明 Redis 消息已足够，不从 DB 截取任何

            // 合并去重（保持插入顺序）
            Map<String, ChatMessage> merged = new LinkedHashMap<>();
            for (ChatMessage m : dbMsgs)    merged.put(m.getMessageId(), m);
            for (ChatMessage m : redisMsgs) merged.put(m.getMessageId(), m);

            List<ChatMessage> finalList = new ArrayList<>(merged.values());
            // 最终按时间升序
            finalList.sort(Comparator.comparing(ChatMessage::getTimestamp));

            // 附件注入
            List<String> mids = finalList.stream()
                    .map(ChatMessage::getMessageId)
                    .toList();
            if (!mids.isEmpty()) {
                List<Attachment> atts = attachmentMapper.selectByMessageIds(mids);
                Map<String, List<Attachment>> attMap = atts.stream()
                        .collect(Collectors.groupingBy(Attachment::getMessageId));
                finalList.forEach(m ->
                        m.setAttachment(attMap.getOrDefault(m.getMessageId(), List.of()))
                );
            }

            result.put(roomId, finalList);
        }

        return result;
    }



}
