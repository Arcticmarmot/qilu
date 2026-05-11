package com.marmot.qilu.modules.notification.sse.controller;

import com.marmot.qilu.modules.notification.sse.service.NotificationSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

@Tag(name = "NotificationSse", description = "通知中心 SSE 实时推送接口")
@RestController
@RequestMapping("/notification-sse")
@RequiredArgsConstructor
public class NotificationSseController {

    private final NotificationSseService notificationSseService;

    @Operation(summary = "建立通知中心 SSE 连接")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        return notificationSseService.connect();
    }
}
