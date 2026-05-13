package com.marmot.qilu.common.event.voucher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_VOUCHER_ORDER_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderProducer {

    private final KafkaTemplate<String, VoucherOrderEvent> kafkaTemplate;

    public void sendVoucherOrderEvent(VoucherOrderEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("voucher order event must not be null");
        }

        String bizKey = event.getUserUuid();

        kafkaTemplate.send(TOPIC_VOUCHER_ORDER_EVENTS, bizKey, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("send voucher order event failed, eventId={}, seckillId={}, voucherId={}, userUuid={}",
                            event.getEventId(), event.getSeckillId(), event.getVoucherId(), event.getUserUuid(), ex);
                    return;
                }

                log.info("send voucher order event success, eventId={}, seckillId={}, voucherId={}, userUuid={}",
                        event.getEventId(), event.getSeckillId(), event.getVoucherId(), event.getUserUuid());
            }
        );
    }
}
