package com.sdu.factorymonitor.controller;

import com.sdu.factorymonitor.dto.AlarmRecordResponse;
import com.sdu.factorymonitor.enums.AlarmStatus;
import com.sdu.factorymonitor.enums.AlarmType;
import com.sdu.factorymonitor.service.MonitoringService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final MonitoringService monitoringService;

    @GetMapping
    public List<AlarmRecordResponse> queryAlarms(
            @RequestParam(required = false) String sensorCode,
            @RequestParam(required = false) AlarmType alarmType,
            @RequestParam(required = false) AlarmStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return monitoringService.queryAlarms(sensorCode, alarmType, status, startTime, endTime);
    }
}
