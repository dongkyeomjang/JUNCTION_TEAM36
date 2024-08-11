package com.junction.smart_blinker.dto.response;

import com.junction.smart_blinker.domain.Blinker;
import lombok.Builder;

@Builder
public record BlinkerColorDto(
        Long id,
        Integer status
) {
    public static BlinkerColorDto fromEntity(Blinker blinker) {
        return BlinkerColorDto.builder()
                .id(blinker.getId())
                .status(blinker.getStatus())
                .build();
    }
}
