package cn.anoxia.chat.core.server;

import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.enmu.MessageStatus;
import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisServer {

    @Getter
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DefaultRedisScript<String> idGeneratorScript;


    /**
     * 生成全局唯一 ID（每日重置）
     */
    public String generateDailyId() {
        String id = redisTemplate.execute(idGeneratorScript, new StringRedisSerializer(), new StringRedisSerializer(), Collections.emptyList());
        if (id == null) {
            log.error("生成 Daily ID 失败，Lua 脚本返回 null");
            throw new IllegalStateException("无法生成 Daily ID，请检查 Redis 或脚本配置");
        }
        return id;
    }

    /**
     * 缓存所有正常消息（包括自己发和别人发）
     */
    public void cacheMessage(ChatMessage msg) {
//        String normalKey = "normal:messages:" + msg.getRoom().getRoomId();
        String normalKey = "normal:messages:";
        String msgJson = JSON.toJSONString(msg);

        // 使用消息 ID 作为 Hash 键来存储消息
        String messageId = msg.getMessageId(); // 假设每条消息都有一个唯一的 messageId

        redisTemplate.opsForHash().put(normalKey, messageId, msgJson);

        // 保留最近 24 小时消息
        redisTemplate.expire(normalKey, 24, TimeUnit.HOURS);
    }

    /**
     * 获取历史消息列表（正常缓存，不删除）
     */
    public List<ChatMessage> getMessages(Long roomId, int start, int end) {
        String normalKey = "normal:messages:" + roomId;

        // 获取 Hash 中的所有消息 ID
        List<Object> raw = redisTemplate.opsForHash().values(normalKey);
        log.info("获取Redis缓存：{}{}", normalKey, raw.size());
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatMessage> msgs = new ArrayList<>(raw.size());
        for (Object o : raw) {
            msgs.add(JSON.parseObject((String) o, ChatMessage.class));
        }

        //消息排序
        msgs.sort(Comparator.comparing(ChatMessage::getMessageId));

        int size = msgs.size();
        int fromIndex = Math.max(0, Math.min(start, size));
        int toIndex = Math.max(0, Math.min(end, size));
        return msgs.subList(fromIndex, toIndex);
    }

    /**
     * 批量获取聊天室最后一条消息
     */
    public List<ChatMessage> getLastMessagesList(List<Long> roomIds) {
        List<ChatMessage> latestMessages = new ArrayList<>();
        for (Long roomId : roomIds) {
            String key = "normal:messages:" + roomId;
            Map<Object, Object> messages = redisTemplate.opsForHash().entries(key);
            log.info("{}-{}", messages, messages.size());

            Optional<ChatMessage> latest = messages.values().stream()
                    .map(object -> JSON.parseObject((String) object, ChatMessage.class))
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(ChatMessage::getTimestamp));

            latest.ifPresent(latestMessages::add);
        }
        return latestMessages;
    }

    /**
     * 批量修改指定房间中多个消息的状态
     *
     * @param roomId                  房间 ID
     * @param messageIdToNewStatusMap Map，key 是要修改的消息 ID，value 是新的 MessageStatus
     * @return 更新成功的消息 ID 列表
     */
    public List<String> batchUpdateMessageStatus(Long roomId, Map<String, MessageStatus> messageIdToNewStatusMap) {
        String normalKey = "normal:messages:" + roomId;
        List<Object> messageIdsToUpdate = new ArrayList<>(messageIdToNewStatusMap.keySet());

        // 1. 批量获取需要更新的消息 JSON 字符串
        List<Object> rawMessages = redisTemplate.opsForHash().multiGet(normalKey, messageIdsToUpdate);
        if (rawMessages.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> updatedMessageJsonMap = new HashMap<>();
        List<String> updatedMessageIds = new ArrayList<>();

        // 2. 遍历获取到的消息，更新状态并序列化
        for (int i = 0; i < messageIdsToUpdate.size(); i++) {
            String messageId = (String) messageIdsToUpdate.get(i);
            Object rawMessage = rawMessages.get(i);

            if (rawMessage != null) {
                try {
                    ChatMessage message = JSON.parseObject((String) rawMessage, ChatMessage.class);
                    MessageStatus newStatus = messageIdToNewStatusMap.get(messageId);
                    if (newStatus != null) {
                        message.setStatus(newStatus);
                        updatedMessageJsonMap.put(messageId, JSON.toJSONString(message));
                        updatedMessageIds.add(messageId);
                    }
                } catch (Exception e) {
                    log.error("批量更新消息状态时解析 JSON 失败，messageId: {}", messageId, e);
                }
            }
        }

        // 3. 批量将更新后的消息 JSON 字符串写回 Redis
        if (!updatedMessageJsonMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(normalKey, updatedMessageJsonMap);
        }

        return updatedMessageIds;
    }

    /**
     * 安全地扫描所有匹配 pattern 的 key，替代 keys(pattern)
     * @param pattern e.g. "normal:messages:*"
     */
    public Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();

            // 构建 ScanOptions
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(500)    // 每次迭代建议设置成合理的批量大小
                    .build();

            // 从 RedisConnection 中获取 RedisKeyCommands
            RedisKeyCommands keyCommands = connection.keyCommands();

            // 非阻塞式迭代游标
            try (Cursor<byte[]> cursor = keyCommands.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }

            return keys;
        });
    }
}