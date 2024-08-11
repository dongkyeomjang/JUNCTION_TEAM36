package com.junction.smart_blinker.domain;

import com.junction.smart_blinker.constants.Constant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blinker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "longitude")
    Double longitude;

    @Column(name = "latitude")
    Double latitude;

    @Column(name = "region_name1")
    String regionName1;

    @Column(name = "region_name2")
    String regionName2;

    @Column(name = "region_name3")
    String regionName3;

    @Column(name = "region_name4")
    String regionName4;

    @Column(name = "road_addr")
    String roadAddr;

    @Column(name = "road_addr_num")
    Integer roadAddrNum;

    @Column(name = "status")
    Integer status;

    @Column(name = "current_mode")
    Integer currentMode;

    @Column(name = "allocated_second")
    Integer allocatedSecond;

    @Column(name = "last_redsignal_time")
    LocalDateTime lastRedSignalTime;

    @Column(name = "type")
    Integer type;

    @Builder
    public Blinker(
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
            Integer type) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.regionName1 = regionName1;
        this.regionName2 = regionName2;
        this.regionName3 = regionName3;
        this.regionName4 = regionName4;
        this.roadAddr = roadAddr;
        this.roadAddrNum = roadAddrNum;
        this.status = status;
        this.currentMode = currentMode;
        this.lastRedSignalTime = LocalDateTime.now();
        this.type = type;
    }

    public void updateStatus(int newStatus) {
        if (newStatus == Constant.RED) {
            lastRedSignalTime = LocalDateTime.now(); // 빨간불로 변경된 시간을 저장
        }
        this.status = newStatus;
    }

    public void updateCurrentMode(Integer currentMode) {
        this.currentMode = currentMode;
    }
}
