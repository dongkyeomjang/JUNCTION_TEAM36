package com.junction.smart_blinker.dto.response;

import com.junction.smart_blinker.domain.Blinker;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public record BlinkerDto(
        Long id,
        Double longitude,
        Double latitude,
        String regionName1,
        String regionName2,
        String regionName3,
        String regionName4,
        String roadAddr,
        Integer roadAddrNum,
        Integer status,
        Integer currentMode,
        Integer allocatedSecond,
        Integer type,
        Double complexity
) {
    public static BlinkerDto fromEntity(Blinker blinker) {
        return BlinkerDto.builder()
                .id(blinker.getId())
                .longitude(blinker.getLongitude())
                .latitude(blinker.getLatitude())
                .regionName1(blinker.getRegionName1())
                .regionName2(blinker.getRegionName2())
                .regionName3(blinker.getRegionName3())
                .regionName4(blinker.getRegionName4())
                .roadAddr(blinker.getRoadAddr())
                .roadAddrNum(blinker.getRoadAddrNum())
                .status(blinker.getStatus())
                .currentMode(blinker.getCurrentMode())
                .allocatedSecond(blinker.getAllocatedSecond())
                .type(blinker.getType())
                .complexity(blinker.getComplexity())
                .build();
    }
}
