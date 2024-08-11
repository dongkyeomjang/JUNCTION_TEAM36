package com.junction.smart_blinker;

import com.junction.smart_blinker.constants.Constant;
import com.junction.smart_blinker.domain.Blinker;
import com.junction.smart_blinker.repository.BlinkerRepository;
import com.junction.smart_blinker.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplicationStartup {

    private final TrafficService trafficService;
    private final BlinkerRepository blinkerRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        List<Blinker> allBlinkers = trafficService.getAllBlinkers();
        for (Blinker blinker : allBlinkers) {
            blinker.updateCurrentMode(Constant.AUTOMODE);
            if (blinker.getId().equals(1L) ||
            blinker.getId().equals(2L) ||
            blinker.getId().equals(5L) ||
            blinker.getId().equals(6L) ||
            blinker.getId().equals(9L) ||
            blinker.getId().equals(10L)) {
                blinker.updateStatus(Constant.GREEN);
                blinkerRepository.save(blinker);
            } else {
                blinker.updateStatus(Constant.RED);
                blinkerRepository.save(blinker);
            }
            trafficService.startAutoMode(blinker);
        }
    }
}
