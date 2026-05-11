package com.marmot.qilu.modules.hot.job;

import com.marmot.qilu.modules.hot.service.HotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotPostRankJob {

    private final HotPostService hotPostService;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildHotPostRankOnStartup() {
        hotPostService.rebuildHotPostRank();
    }

    @Scheduled(fixedDelay = 3 * 60 * 1000, initialDelay = 60 * 1000)
    public void rebuildPostRank() {
        hotPostService.rebuildHotPostRank();
    }

}
