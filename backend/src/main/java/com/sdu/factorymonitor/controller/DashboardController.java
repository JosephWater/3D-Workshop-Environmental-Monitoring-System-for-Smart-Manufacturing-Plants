package com.sdu.factorymonitor.controller;

import com.sdu.factorymonitor.dto.DashboardSummaryResponse;
import com.sdu.factorymonitor.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MonitoringService monitoringService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return monitoringService.getDashboardSummary();
    }
}
