package com.marmot.qilu.modules.voucher.consumer;

import com.marmot.qilu.common.event.voucher.VoucherOrderEvent;
import com.marmot.qilu.modules.voucher.service.VoucherOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_VOUCHER_ORDER_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderConsumer {

    private static final String GROUP_VOUCHER_GROUP = "qilu-voucher-order-group";

    private final VoucherOrderService voucherOrderService;

    @KafkaListener(
            topics = TOPIC_VOUCHER_ORDER_EVENTS,
            groupId = GROUP_VOUCHER_GROUP
    )
    public void onVoucherOrderEvent(VoucherOrderEvent event, Acknowledgment acknowledgment) {
        validateVoucherOrderEvent(event);

        try {
            Long voucherOrderId = voucherOrderService.createVoucherOrder(
                    event.getUserUuid(),
                    event.getVoucherId(),
                    event.getSeckillId()
            );
            acknowledgment.acknowledge();
            log.debug("consume voucher order event success, eventId={}, orderId={}", event.getEventId(), voucherOrderId);
        } catch (Exception e) {
            log.error("consume voucher order event failed, eventId={}, seckillId={}, voucherId={}, userUuid={}",
                    event.getEventId(), event.getSeckillId(), event.getVoucherId(), event.getUserUuid(), e);
            throw e;
        }
    }

    private void validateVoucherOrderEvent(VoucherOrderEvent event) {
        if (event == null) {
            log.error("consume voucher order event failed, event is null");
            throw new IllegalArgumentException("voucher order event must not be null");
        }

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            log.error("consume voucher order event failed, eventId is blank");
            throw new IllegalArgumentException("voucher order event id must not be blank");
        }

        if (event.getUserUuid() == null || event.getUserUuid().isBlank()) {
            log.error("consume voucher order event failed, eventId={}, userUuid is blank", event.getEventId());
            throw new IllegalArgumentException("voucher order event user uuid must not be blank");
        }

        if (event.getVoucherId() == null || event.getVoucherId() <= 0) {
            log.error("consume voucher order event failed, eventId={}, voucherId={}",
                    event.getEventId(), event.getVoucherId());
            throw new IllegalArgumentException("voucher order event voucher id is invalid");
        }

        if (event.getSeckillId() == null || event.getSeckillId() <= 0) {
            log.error("consume voucher order event failed, eventId={}, seckillId={}",
                    event.getEventId(), event.getSeckillId());
            throw new IllegalArgumentException("voucher order event seckill id is invalid");
        }
    }

}
