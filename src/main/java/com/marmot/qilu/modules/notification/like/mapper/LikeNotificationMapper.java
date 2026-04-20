package com.marmot.qilu.modules.notification.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.notification.like.entity.LikeNotification;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeNotificationMapper extends BaseMapper<LikeNotification> {

    List<LikeNotificationListItemVO> selectLikeNotifications(@Param("currUserUuid") String currUserUuid);

    int markLikeNotificationsRead(@Param("currUserUuid") String currUserUuid);
}
