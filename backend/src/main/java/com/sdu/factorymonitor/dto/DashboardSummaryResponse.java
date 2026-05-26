package com.sdu.factorymonitor.dto;

import java.time.LocalDateTime;

public record DashboardSummaryResponse(
        long totalSensors,
        long totalDataRows,
        long activeAlarmCount,
        long resolvedAlarmCount,
        LocalDateTime latestCollectTime
) {
}
