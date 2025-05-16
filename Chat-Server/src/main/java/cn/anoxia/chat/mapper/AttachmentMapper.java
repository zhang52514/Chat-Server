package cn.anoxia.chat.mapper;

import cn.anoxia.chat.common.domain.Attachment;
import cn.anoxia.chat.common.domain.AttachmentDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttachmentMapper {
    int batchInsertMessages(List<Attachment> attachment);
    List<Attachment> selectByMessageIds(List<String> messageIds);

    AttachmentDetail selectAttachmentDetailByMessageId(String id);
}
