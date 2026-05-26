package com.sdu.factorymonitor.dto;

import com.sdu.factorymonitor.enums.AlarmLevel;
import com.sdu.factorymonitor.enums.AlarmStatus;
import com.sdu.factorymonitor.enums.AlarmType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlarmRecordResponse(
        Long id,
        String sensorCode,
        String sensorName,
        AlarmType alarmType,
        BigDecimal currentValue,
        String thresholdDesc,
        AlarmLevel alarmLevel,
        AlarmStatus status,
        LocalDateTime alarmTime,
        LocalDateTime resolvedAt
) {
}
