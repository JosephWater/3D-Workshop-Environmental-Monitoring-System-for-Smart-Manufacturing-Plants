package com.sdu.factorymonitor.service;

import com.sdu.factorymonitor.dto.AlarmRuleRequest;
import com.sdu.factorymonitor.dto.AlarmRuleResponse;
import com.sdu.factorymonitor.entity.AlarmRule;
import com.sdu.factorymonitor.repository.AlarmRuleRepository;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlarmRuleService {

    private final AlarmRuleRepository alarmRuleRepository;

    @Transactional(readOnly = true)
    public AlarmRuleResponse getRule() {
        return toResponse(getCurrentRuleEntity());
    }

    @Transactional(readOnly = true)
    public AlarmRule getCurrentRuleEntity() {
        return alarmRuleRepository.findAll().stream()
                .max(Comparator.comparing(AlarmRule::getId))
                .orElseThrow(() -> new IllegalArgumentException("未找到告警规则"));
    }

    @Transactional
    public AlarmRuleResponse updateRule(AlarmRuleRequest request) {
        if (request.tempMin().compareTo(request.tempMax()) > 0) {
            throw new IllegalArgumentException("温度下限不能大于上限");
        }
        if (request.humidityMin().compareTo(request.humidityMax()) > 0) {
            throw new IllegalArgumentException("湿度下限不能大于上限");
        }

        AlarmRule rule = getCurrentRuleEntity();
        rule.setTempMin(request.tempMin());
        rule.setTempMax(request.tempMax());
        rule.setHumidityMin(request.humidityMin());
        rule.setHumidityMax(request.humidityMax());
        rule.setEnabled(request.enabled());
        return toResponse(alarmRuleRepository.save(rule));
    }

    private AlarmRuleResponse toResponse(AlarmRule rule) {
        return new AlarmRuleResponse(
                rule.getId(),
                rule.getTempMin(),
                rule.getTempMax(),
                rule.getHumidityMin(),
                rule.getHumidityMax(),
                rule.getEnabled(),
                rule.getUpdatedAt()
        );
    }
}
