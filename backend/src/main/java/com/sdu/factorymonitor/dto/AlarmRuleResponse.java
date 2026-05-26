package com.sdu.factorymonitor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlarmRuleResponse(
        Long id,
        BigDecimal tempMin,
        BigDecimal tempMax,
        BigDecimal humidityMin,
        BigDecimal humidityMax,
        Boolean enabled,
        LocalDateTime updatedAt
) {
}
