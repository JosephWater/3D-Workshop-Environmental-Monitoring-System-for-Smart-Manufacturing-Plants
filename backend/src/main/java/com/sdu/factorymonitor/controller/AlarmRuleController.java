package com.sdu.factorymonitor.controller;

import com.sdu.factorymonitor.dto.AlarmRuleRequest;
import com.sdu.factorymonitor.dto.AlarmRuleResponse;
import com.sdu.factorymonitor.service.AlarmRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarm-rule")
@RequiredArgsConstructor
public class AlarmRuleController {

    private final AlarmRuleService alarmRuleService;

    @GetMapping
    public AlarmRuleResponse getRule() {
        return alarmRuleService.getRule();
    }

    @PutMapping
    public AlarmRuleResponse updateRule(@Valid @RequestBody AlarmRuleRequest request) {
        return alarmRuleService.updateRule(request);
    }
}
