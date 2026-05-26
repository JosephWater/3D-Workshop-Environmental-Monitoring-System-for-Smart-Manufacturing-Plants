package com.sdu.factorymonitor.dto;

import com.sdu.factorymonitor.enums.AlarmType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record LatestSensorResponse(
        String sensorCode,
        String sensorName,
        String gridPosition,
        double x,
        double y,
        double z,
        BigDecimal temperature,
        BigDecimal humidity,
        LocalDateTime collectTime,
        boolean alarmActive,
        List<AlarmType> alarmTypes
) {
}
