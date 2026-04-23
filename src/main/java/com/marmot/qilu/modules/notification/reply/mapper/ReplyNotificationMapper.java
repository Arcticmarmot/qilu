package com.marmot.qilu.modules.notification.reply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.notification.reply.entity.ReplyNotification;
import com.marmot.qilu.modules.notification.reply.vo.ReplyNotificationListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReplyNotificationMapper extends BaseMapper<ReplyNotification> {

    List<ReplyNotificationListItemVO> selectReplyNotifications(@Param("currUserUuid") String currUserUuid,
                                                               @Param("previewLength") int previewLength);

    int updateReplyNotificationsRead(@Param("currUserUuid") String currUserUuid);

    int countUnreadReplyNotifications(@Param("currUserUuid") String currUserUuid);
}
