package com.junction.smart_blinker.service;

import com.junction.smart_blinker.constants.Constant;
import com.junction.smart_blinker.domain.Blinker;
import com.junction.smart_blinker.dto.request.TrafficDto;
import com.junction.smart_blinker.dto.response.BlinkerColorDto;
import com.junction.smart_blinker.exception.CommonException;
import com.junction.smart_blinker.exception.ErrorCode;
import com.junction.smart_blinker.repository.BlinkerRelationRepository;
import com.junction.smart_blinker.repository.BlinkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficService {
    private final BlinkerRepository blinkerRepository;
    private final BlinkerRelationRepository blinkerRelationRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private ScheduledFuture<?> autoModeTask;
    private final SimpMessagingTemplate messagingTemplate;

    public List<Blinker> getAllBlinkers() {
        return blinkerRepository.findAll();
    }

    // 자동모드일 경우, 기본적으로 항상 실행되고있는 상태여야 함. 매뉴얼모드가 실행되면 중지.
    @Async
    public void startAutoMode(Blinker blinker) {
        autoModeTask = taskScheduler.scheduleAtFixedRate(() -> {
            // 자동 모드에서 주기적으로 실행되는 로직
            if (blinker.getCurrentMode().equals(Constant.AUTOMODE)) { // 현재 모드가 오토모드라면,
                if (blinker.getStatus().equals(Constant.GREEN)) { // 현재 신호가 초록불인 경우
                    blinker.updateStatus(Constant.YELLOW); // 현재 신호를 노란불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                    blinkerRepository.save(blinker);
                    try {
                        Thread.sleep(2000); // 2초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (blinker.getType().equals(Constant.FOURCOLOR)) { // 4색 신호등인 경우
                        blinker.updateStatus(Constant.LEFTTRUNRED); // 현재 신호를 좌회전 신호로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                        blinkerRepository.save(blinker);
                        try{
                            Thread.sleep(2000); // 2초 대기
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        blinker.updateStatus(Constant.REDYELLOW); // 현재 신호를 빨간노란불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                        blinkerRepository.save(blinker);
                        try{
                            Thread.sleep(2000); // 2초 대기
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        blinker.updateStatus(Constant.RED); // 현재 신호를 빨간불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                        blinkerRepository.save(blinker);
                    } else{
                        blinker.updateStatus(Constant.RED); // 현재 신호를 빨간불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                        blinkerRepository.save(blinker);
                    }
                } else { // 현재 신호가 빨간불인 경우
                    try{
                        Thread.sleep(4000); // 4초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    blinker.updateStatus(Constant.GREEN); // 현재 신호를 초록불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                    blinkerRepository.save(blinker);
                }
            }
        }, blinker.getAllocatedSecond()*1000); // 주어진 할당 시간마다 실행.
    }

    public void stopAutoMode() {
        if (autoModeTask != null && !autoModeTask.isDone()) {
            // 진행 중이던 작업 취소
            boolean cancelled = autoModeTask.cancel(true);
            log.info("Auto mode task cancelled: " + cancelled);
            if (!cancelled) {
                log.warn("Auto mode task was not successfully cancelled");
            } else {
                // 작업 취소가 성공적이라면 스케줄러에서 모든 대기 중인 작업도 취소
                taskScheduler.shutdown();
                try {
                    // 스케줄러가 완전히 종료되기를 기다림
                    taskScheduler.getScheduledExecutor().awaitTermination(5, TimeUnit.SECONDS);
                    log.info("Scheduler shutdown complete.");
                } catch (InterruptedException e) {
                    log.error("Error during scheduler shutdown: ", e);
                }
                // 스케줄러를 재시작
                taskScheduler.initialize();
                scheduleAutoModeSwitch(1, TimeUnit.MINUTES);
            }
        }
    }


    // 밀도가 0.5 이상일 경우 매뉴얼모드(차량우선모드)로 변경, 0.5 미만일 경우 자동모드로 변경
    @Async
    public void consumeTrafficInformation(TrafficDto trafficDto) {
        Blinker blinker = blinkerRepository.findById(trafficDto.blinkerId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));
        blinker.updateComplexity(trafficDto.carDensity());
        blinkerRepository.save(blinker);
        List<Blinker> allBlinkers = getAllBlinkers();
        List<Blinker> parallelBlinkers = blinkerRelationRepository.findParallelBlinkers(blinker.getId());
        List<Blinker> crossBlinkers = blinkerRelationRepository.findCrossBlinkers(blinker.getId());
        List<Blinker> nextBlinkers = blinkerRelationRepository.findNextBlinkers(blinker.getId());
        // 현재 모드가 자동 모드이고, 차량 밀도가 0.5 이상일 경우
        if (blinker.getCurrentMode().equals(Constant.AUTOMODE) && (trafficDto.carDensity() >= 0.5)){
            stopAutoMode(); // 자동 모드 중지
            for (Blinker allBlinker : allBlinkers) {
                allBlinker.updateCurrentMode(Constant.MANUALMODE); // 모든 신호등을 매뉴얼 모드로 변경
                blinker.updateStatus(Constant.YELLOW);
                blinkerRepository.save(blinker);
                sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                try { // 오토 모드에서 매뉴얼 모드로 바뀌기 전, 노란불로 변경 후 2초 대기
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                blinker.updateStatus(Constant.RED);
                blinkerRepository.save(blinker);
                sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                try { // 빨간불로 변경 후 2초 대기
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (blinker.getId().equals(1L) ||
                        blinker.getId().equals(2L) ||
                        blinker.getId().equals(5L) ||
                        blinker.getId().equals(6L) ||
                        blinker.getId().equals(9L) ||
                        blinker.getId().equals(10L)) {
                    blinker.updateStatus(Constant.GREEN);
                    blinkerRepository.save(blinker);
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                } else {
                    blinker.updateStatus(Constant.RED);
                    blinkerRepository.save(blinker);
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                }
            }
        }
        blinker = blinkerRepository.findById(trafficDto.blinkerId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));

        // 현재 모드가 매뉴얼 모드일 때
        // case 1: 초록불인경우
        if (blinker.getCurrentMode().equals(Constant.MANUALMODE) && blinker.getStatus().equals(Constant.GREEN)) {
            if (trafficDto.isPedestrian() || trafficDto.isCrossCar()) { // 보행자가 대기중이거나, 수직한 방향에 차량이 대기중이라면
                // 마지막으로 빨간불이 된지 할당시간만큼 경과되지 않았다면 초록불 유지. 경과했다면 빨간불로 변경
                if (blinker.getLastRedSignalTime() != null &&
                        blinker.getLastRedSignalTime().plusSeconds(blinker.getAllocatedSecond()).isBefore(LocalDateTime.now())) {
                    blinker.updateStatus(Constant.YELLOW); // 현재 신호를 노란불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                    blinkerRepository.save(blinker);
                    for (Blinker nextBlinker : nextBlinkers) {
                        nextBlinker.updateStatus(Constant.YELLOW); // 다음 신호등도 노란불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(nextBlinker));
                        blinkerRepository.save(nextBlinker);
                    }
                    for (Blinker parallelBlinker : parallelBlinkers) {
                        parallelBlinker.updateStatus(Constant.YELLOW); // 평행 신호등도 노란불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(parallelBlinker));
                        blinkerRepository.save(parallelBlinker);
                    }
                    try{
                        Thread.sleep(2000); // 2초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    blinker.updateStatus(Constant.RED);
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                    blinkerRepository.save(blinker);
                    for (Blinker nextBlinker : nextBlinkers) {
                        nextBlinker.updateStatus(Constant.RED); // 다음 신호등도 빨간불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(nextBlinker));
                        blinkerRepository.save(nextBlinker);
                    }
                    for (Blinker parallelBlinker : parallelBlinkers) {
                        parallelBlinker.updateStatus(Constant.RED); // 평행 신호등은 빨간불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(parallelBlinker));
                        blinkerRepository.save(parallelBlinker);
                    }
                    for (Blinker crossBlinker : crossBlinkers) {
                        crossBlinker.updateStatus(Constant.GREEN); // 교차 신호등은 초록불로 변경
                        sendMessageToClient(BlinkerColorDto.fromEntity(crossBlinker));
                        blinkerRepository.save(crossBlinker);
                    }
                }
            }
            // 보행자 혹은 수직 차량이 대기중이 아니면 그대로 초록불 유지
        } else if (blinker.getCurrentMode().equals(Constant.MANUALMODE) && blinker.getStatus().equals(Constant.RED)) {
            // case 2 : 빨간불인경우
            // 보행 신호등이 꺼졌을경우와 켜져있을 경우 나누어 처리. 꺼졌을 경우엔 초록불로 변경, 켜져있을 경우엔 그대로 빨간불 유지
            if (!trafficDto.isPedestrian()) { // 보행자가 없다면
                for (Blinker crossBlinker : crossBlinkers) {
                    crossBlinker.updateStatus(Constant.YELLOW); // 교차 신호등은 노란불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(crossBlinker));
                    blinkerRepository.save(crossBlinker);
                }
                try{
                    Thread.sleep(2000); // 2초 대기
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Blinker crossBlinker : crossBlinkers) {
                    crossBlinker.updateStatus(Constant.RED); // 교차 신호등은 빨간불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(crossBlinker));
                    blinkerRepository.save(crossBlinker);
                }

                blinker.updateStatus(Constant.GREEN); // 현재 신호를 초록불로 변경
                blinkerRepository.save(blinker);
                sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                for (Blinker nextBlinker : nextBlinkers) {
                    nextBlinker.updateStatus(Constant.GREEN); // 다음 신호등도 초록불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(nextBlinker));
                    blinkerRepository.save(nextBlinker);
                }
                for (Blinker parallelBlinker : parallelBlinkers) {
                    parallelBlinker.updateStatus(Constant.GREEN); // 평행 신호등도 초록불로 변경
                    sendMessageToClient(BlinkerColorDto.fromEntity(parallelBlinker));
                    blinkerRepository.save(parallelBlinker);
                }
            }
            // 보행자가 있다면 그대로 빨간불 유지
        }
    }
    @Async
    public void scheduleAutoModeSwitch(long delay, TimeUnit unit) {
        autoModeTask = taskScheduler.schedule(() -> {
            List<Blinker> allBlinkers = getAllBlinkers();
            for (Blinker blinker : allBlinkers) {
                blinker.updateCurrentMode(Constant.AUTOMODE);
                blinker.updateStatus(Constant.YELLOW);
                blinkerRepository.save(blinker);
                sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                try { // 매뉴얼 모드에서 오토 모드로 바뀌기 전, 노란불로 변경 후 2초 대기
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Blinker blinker : allBlinkers) {
                blinker.updateStatus(Constant.RED);
                blinkerRepository.save(blinker);
                sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                try { // 빨간불로 변경 후 2초 대기
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Blinker blinker : allBlinkers) {
                if (blinker.getId().equals(1L) ||
                        blinker.getId().equals(2L) ||
                        blinker.getId().equals(5L) ||
                        blinker.getId().equals(6L) ||
                        blinker.getId().equals(9L) ||
                        blinker.getId().equals(10L)) {
                    blinker.updateStatus(Constant.GREEN);
                    blinkerRepository.save(blinker);
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                } else {
                    blinker.updateStatus(Constant.RED);
                    blinkerRepository.save(blinker);
                    sendMessageToClient(BlinkerColorDto.fromEntity(blinker));
                }
                startAutoMode(blinker);
            }
        }, new Date(System.currentTimeMillis() + unit.toMillis(delay)));
    }

    private void sendMessageToClient(BlinkerColorDto blinkerColorDto) {
        messagingTemplate.convertAndSend("/subscribe/blinkers", blinkerColorDto);
    }
}
