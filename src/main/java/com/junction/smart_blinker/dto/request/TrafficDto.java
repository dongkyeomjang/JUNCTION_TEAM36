package com.junction.smart_blinker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrafficDto(
        @JsonProperty(value = "is_pedestrian")
        Boolean isPedestrian,
        @JsonProperty(value = "is_cross_car")
        Boolean isCrossCar,
        @JsonProperty(value = "is_left_car")
        Boolean isLeftCar,
        @JsonProperty(value = "car_density")
        Double carDensity,
        @JsonProperty(value = "blinker_id")
        Long blinkerId
) {
}
