package com.junction.smart_blinker.controller;

import com.junction.smart_blinker.dto.global.ResponseDto;
import com.junction.smart_blinker.dto.request.TrafficDto;
import com.junction.smart_blinker.service.TrafficService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrafficController {
    private final TrafficService trafficService;
    @PostMapping("/api/v1/traffic")
    public ResponseDto<?> consumeTrafficInformation(@RequestBody TrafficDto trafficDto) {
        trafficService.consumeTrafficInformation(trafficDto);
        return ResponseDto.ok(null);
    }
}
