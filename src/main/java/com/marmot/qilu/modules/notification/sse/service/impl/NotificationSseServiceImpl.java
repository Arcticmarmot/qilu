package com.marmot.qilu.modules.notification.sse.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.modules.notification.enums.NotificationType;
import com.marmot.qilu.modules.notification.sse.service.NotificationSseService;
import com.marmot.qilu.modules.notification.sse.vo.NotificationSseEventVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import static com.marmot.qilu.modules.notification.sse.enums.NotificationSseEventType.CONNECTED;
import static com.marmot.qilu.modules.notification.sse.enums.NotificationSseEventType.NOTIFICATION_CREATED;

@Slf4j
@Service
public class NotificationSseServiceImpl implements NotificationSseService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000;

    private final ConcurrentHashMap<String, Set<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect() {
        String currUserUuid = UserContext.requireUuid();

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitterMap.computeIfAbsent(currUserUuid, key -> new CopyOnWriteArraySet<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(currUserUuid, emitter));
        emitter.onTimeout(() -> removeEmitter(currUserUuid, emitter));
        emitter.onError(e -> {
            removeEmitter(currUserUuid, emitter);
            log.debug("notification sse connection error, userUuid={}", currUserUuid);
        });

        try {
            emitter.send(SseEmitter.event().name("connected")
                    .data(new NotificationSseEventVO(CONNECTED.getValue(), null)));
        } catch (Exception e) {
            removeEmitter(currUserUuid, emitter);
            log.warn("send notification sse connected event failed, userUuid={}", currUserUuid);
        }
        log.info("notification sse connect success, userUuid={}", currUserUuid);
        return emitter;
    }

    @Override
    public void sendNotificationCreated(String receiverUuid, NotificationType notificationType) {
        if(receiverUuid == null) {
            throw new IllegalArgumentException("receiver uuid must not be blank");
        }

        if (notificationType == null) {
            throw new IllegalArgumentException("notification type must not be blank");
        }

        Set<SseEmitter> emitters = emitterMap.get(receiverUuid);
        if(emitters == null || emitters.isEmpty()) {
            return;
        }

        NotificationSseEventVO vo =
                new NotificationSseEventVO(NOTIFICATION_CREATED.getValue(), notificationType.getValue());

        for(SseEmitter emitter: emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification_created").data(vo));
            } catch (Exception e) {
                removeEmitter(receiverUuid, emitter);
                log.warn(
                        "send notification sse event failed, receiverUuid={}, notificationType={}",
                        receiverUuid,
                        notificationType
                );
            }
        }
    }

    private void removeEmitter(String userUuid, SseEmitter emitter) {
        Set<SseEmitter> emitters = emitterMap.get(userUuid);
        if(emitters == null) {
            return;
        }

        if(emitters.remove(emitter)) {
            log.debug(
                    "emitter removed, userUuid={}, emitter={}",
                    userUuid,
                    emitter
            );
        }

        if(emitters.isEmpty()) {
            emitterMap.remove(userUuid);
        }
    }

}
