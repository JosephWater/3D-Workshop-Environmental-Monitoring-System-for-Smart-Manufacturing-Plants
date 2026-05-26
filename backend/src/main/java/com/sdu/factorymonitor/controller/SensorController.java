package com.sdu.factorymonitor.controller;

import com.sdu.factorymonitor.dto.LatestSensorResponse;
import com.sdu.factorymonitor.dto.SensorDataResponse;
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
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final MonitoringService monitoringService;

    @GetMapping("/latest")
    public List<LatestSensorResponse> getLatestSensors() {
        return monitoringService.getLatestSensorStatuses();
    }

    @GetMapping("/history")
    public List<SensorDataResponse> getSensorHistory(
            @RequestParam(required = false) String sensorCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return monitoringService.queryHistory(sensorCode, startTime, endTime);
    }
}
