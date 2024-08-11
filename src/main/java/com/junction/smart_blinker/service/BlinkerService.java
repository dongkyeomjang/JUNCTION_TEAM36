package com.junction.smart_blinker.service;

import com.junction.smart_blinker.domain.Blinker;
import com.junction.smart_blinker.dto.response.BlinkerColorDto;
import com.junction.smart_blinker.dto.response.BlinkerDto;
import com.junction.smart_blinker.exception.CommonException;
import com.junction.smart_blinker.exception.ErrorCode;
import com.junction.smart_blinker.repository.BlinkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlinkerService {
    private final BlinkerRepository blinkerRepository;

    public List<BlinkerDto> getBlinkers() {
        List<Blinker> blinkers = blinkerRepository.findAll();
        return blinkers.stream()
                .map(BlinkerDto::fromEntity)
                .toList();
    }
    public BlinkerDto getBlinkerDetail(Long blinkerId) {

        return BlinkerDto.fromEntity(blinkerRepository.findById(blinkerId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE)));
    }
}
