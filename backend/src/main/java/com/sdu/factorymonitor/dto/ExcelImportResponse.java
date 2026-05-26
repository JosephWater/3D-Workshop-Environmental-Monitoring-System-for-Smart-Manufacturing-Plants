package com.sdu.factorymonitor.dto;

import java.time.LocalDateTime;

public record ExcelImportResponse(
        int importedRows,
        int newAlarmCount,
        String sourceFile,
        LocalDateTime importedAt
) {
}
