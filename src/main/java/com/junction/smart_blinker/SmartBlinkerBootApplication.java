package com.junction.smart_blinker;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class SmartBlinkerBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartBlinkerBootApplication.class, args);
    }

}
