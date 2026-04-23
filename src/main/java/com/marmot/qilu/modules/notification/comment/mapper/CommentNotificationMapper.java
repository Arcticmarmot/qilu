package com.marmot.qilu.modules.notification.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.notification.comment.entity.CommentNotification;
import com.marmot.qilu.modules.notification.comment.vo.CommentNotificationListItemVO;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentNotificationMapper extends BaseMapper<CommentNotification> {

    List<CommentNotificationListItemVO> selectCommentNotifications(@Param("currUserUuid") String currUserUuid,
                                                                   @Param("previewLength") int previewLength);

    int updateCommentNotificationsRead(@Param("currUserUuid") String currUserUuid);

    int countUnreadCommentNotifications(@Param("currUserUuid") String currUserUuid);
}
