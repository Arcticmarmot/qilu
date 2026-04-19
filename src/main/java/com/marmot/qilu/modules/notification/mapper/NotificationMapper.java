package com.marmot.qilu.modules.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.notification.entity.Notification;
import com.marmot.qilu.modules.notification.vo.NotificationListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    List<NotificationListItemVO> selectNotificationsByType(@Param("currUserUuid") String currUserUuid,
                                                          @Param("type") String type);

    int markNotificationsReadByType(@Param("currUserUuid") String currUserUuid,
                                   @Param("type") String type);
}
