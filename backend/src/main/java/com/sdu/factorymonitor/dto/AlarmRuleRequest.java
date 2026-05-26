package com.sdu.factorymonitor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AlarmRuleRequest(
        @NotNull @DecimalMin("0.0") BigDecimal tempMin,
        @NotNull @DecimalMin("0.0") BigDecimal tempMax,
        @NotNull @DecimalMin("0.0") BigDecimal humidityMin,
        @NotNull @DecimalMin("0.0") BigDecimal humidityMax,
        @NotNull Boolean enabled
) {
}
