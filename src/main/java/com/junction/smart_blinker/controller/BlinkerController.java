package com.junction.smart_blinker.controller;

import com.junction.smart_blinker.dto.global.ResponseDto;
import com.junction.smart_blinker.service.BlinkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BlinkerController {
    private final BlinkerService blinkerService;
    @GetMapping("/api/v1/blinkers")
    public ResponseDto<?> getBlinkers() {
        return ResponseDto.ok(blinkerService.getBlinkers());
    }

    @GetMapping("/api/v1/blinkers/{blinkerId}")
    public ResponseDto<?> getBlinkerDetail(@PathVariable Long blinkerId) {
        return ResponseDto.ok(blinkerService.getBlinkerDetail(blinkerId));
    }
}
