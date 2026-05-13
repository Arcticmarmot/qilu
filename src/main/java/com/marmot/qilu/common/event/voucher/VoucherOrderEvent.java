package com.marmot.qilu.common.event.voucher;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VoucherOrderEvent {

    private String eventId;

    private Long seckillId;

    private Long voucherId;

    private String userUuid;

    private LocalDateTime occurredAt;
}
