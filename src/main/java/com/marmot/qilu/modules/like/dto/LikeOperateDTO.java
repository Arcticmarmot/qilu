package com.marmot.qilu.modules.like.dto;

import com.marmot.qilu.common.event.like.LikeEntityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeOperateDTO {

    Long entityId;

    LikeEntityType entityType;
}
