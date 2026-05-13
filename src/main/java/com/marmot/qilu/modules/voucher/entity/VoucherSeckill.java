package com.marmot.qilu.modules.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("voucher_seckill")
public class VoucherSeckill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long voucherId;

    private Integer totalStock;

    private Integer remainingStock;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime redeemDeadline;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
