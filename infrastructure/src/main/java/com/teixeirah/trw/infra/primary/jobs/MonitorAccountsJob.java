package com.teixeirah.trw.infra.primary.jobs;

import com.teixeirah.trw.application.ports.input.MonitorAccountsInputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
class MonitorAccountsJob {

    private final MonitorAccountsInputPort monitor;

    @Scheduled(fixedRate = 10, timeUnit = SECONDS)
    void run() {
        log.info("Running monitor accounts job.");
        monitor.run();
    }
}
