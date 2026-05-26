package com.sdu.factorymonitor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SensorDataResponse(
        Long id,
        String sensorCode,
        String sensorName,
        BigDecimal temperature,
        BigDecimal humidity,
        LocalDateTime collectTime,
        String sourceFile
) {
}
