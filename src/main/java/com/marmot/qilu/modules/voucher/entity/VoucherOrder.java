package com.marmot.qilu.modules.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("voucher_order")
public class VoucherOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long voucherId;

    private Long seckillId;

    private String userUuid;

    private String redeemCode;

    private Integer status;

    private LocalDateTime expireAt;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
