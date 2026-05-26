package com.sdu.factorymonitor.service;

import com.sdu.factorymonitor.dto.AlarmRecordResponse;
import com.sdu.factorymonitor.dto.DashboardSummaryResponse;
import com.sdu.factorymonitor.dto.LatestSensorResponse;
import com.sdu.factorymonitor.dto.SensorDataResponse;
import com.sdu.factorymonitor.entity.AlarmRecord;
import com.sdu.factorymonitor.entity.AlarmRule;
import com.sdu.factorymonitor.entity.SensorData;
import com.sdu.factorymonitor.entity.SensorInfo;
import com.sdu.factorymonitor.enums.AlarmLevel;
import com.sdu.factorymonitor.enums.AlarmStatus;
import com.sdu.factorymonitor.enums.AlarmType;
import com.sdu.factorymonitor.repository.AlarmRecordRepository;
import com.sdu.factorymonitor.repository.SensorDataRepository;
import com.sdu.factorymonitor.repository.SensorInfoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final SensorInfoRepository sensorInfoRepository;
    private final SensorDataRepository sensorDataRepository;
    private final AlarmRecordRepository alarmRecordRepository;

    @Transactional
    public int evaluateAndPersistAlarms(SensorData sensorData, AlarmRule rule) {
        if (!Boolean.TRUE.equals(rule.getEnabled())) {
            resolveAllActiveAlarms(sensorData.getSensorCode(), sensorData.getCollectTime());
            return 0;
        }

        Map<AlarmType, BigDecimal> triggered = new EnumMap<>(AlarmType.class);
        if (sensorData.getTemperature().compareTo(rule.getTempMax()) > 0) {
            triggered.put(AlarmType.TEMP_HIGH, sensorData.getTemperature());
        }
        if (sensorData.getTemperature().compareTo(rule.getTempMin()) < 0) {
            triggered.put(AlarmType.TEMP_LOW, sensorData.getTemperature());
        }
        if (sensorData.getHumidity().compareTo(rule.getHumidityMax()) > 0) {
            triggered.put(AlarmType.HUM_HIGH, sensorData.getHumidity());
        }
        if (sensorData.getHumidity().compareTo(rule.getHumidityMin()) < 0) {
            triggered.put(AlarmType.HUM_LOW, sensorData.getHumidity());
        }

        List<AlarmRecord> activeAlarms = alarmRecordRepository.findBySensorCodeAndStatus(sensorData.getSensorCode(), AlarmStatus.ACTIVE);
        Map<AlarmType, AlarmRecord> activeAlarmMap = activeAlarms.stream()
                .collect(Collectors.toMap(AlarmRecord::getAlarmType, Function.identity(), (left, right) -> left));

        for (AlarmRecord activeAlarm : activeAlarms) {
            if (!triggered.containsKey(activeAlarm.getAlarmType())) {
                activeAlarm.setStatus(AlarmStatus.RESOLVED);
                activeAlarm.setResolvedAt(sensorData.getCollectTime());
                alarmRecordRepository.save(activeAlarm);
            }
        }

        int created = 0;
        for (Map.Entry<AlarmType, BigDecimal> entry : triggered.entrySet()) {
            AlarmType alarmType = entry.getKey();
            AlarmRecord existing = activeAlarmMap.get(alarmType);
            if (existing != null) {
                existing.setCurrentValue(entry.getValue());
                existing.setAlarmTime(sensorData.getCollectTime());
                existing.setAlarmLevel(resolveLevel(alarmType, entry.getValue(), rule));
                existing.setThresholdDesc(thresholdText(alarmType, rule));
                alarmRecordRepository.save(existing);
                continue;
            }

            alarmRecordRepository.save(AlarmRecord.builder()
                    .sensorCode(sensorData.getSensorCode())
                    .alarmType(alarmType)
                    .currentValue(entry.getValue())
                    .thresholdDesc(thresholdText(alarmType, rule))
                    .alarmLevel(resolveLevel(alarmType, entry.getValue(), rule))
                    .status(AlarmStatus.ACTIVE)
                    .alarmTime(sensorData.getCollectTime())
                    .build());
            created++;
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<LatestSensorResponse> getLatestSensorStatuses() {
        List<SensorInfo> sensors = sensorInfoRepository.findAll().stream()
                .sorted((left, right) -> left.getSensorCode().compareTo(right.getSensorCode()))
                .toList();

        return sensors.stream().map(sensor -> {
            SensorData latest = sensorDataRepository.findFirstBySensorCodeOrderByCollectTimeDescIdDesc(sensor.getSensorCode()).orElse(null);
            List<AlarmType> alarmTypes = alarmRecordRepository.findBySensorCodeAndStatus(sensor.getSensorCode(), AlarmStatus.ACTIVE)
                    .stream()
                    .map(AlarmRecord::getAlarmType)
                    .toList();
            return new LatestSensorResponse(
                    sensor.getSensorCode(),
                    sensor.getSensorName(),
                    sensor.getGridPosition(),
                    sensor.getXPos(),
                    sensor.getYPos(),
                    sensor.getZPos(),
                    latest != null ? latest.getTemperature() : null,
                    latest != null ? latest.getHumidity() : null,
                    latest != null ? latest.getCollectTime() : null,
                    !alarmTypes.isEmpty(),
                    alarmTypes
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<SensorDataResponse> queryHistory(String sensorCode, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, String> sensorNameMap = sensorInfoRepository.findAll().stream()
                .collect(Collectors.toMap(SensorInfo::getSensorCode, SensorInfo::getSensorName));
        return sensorDataRepository.queryHistory(normalizeSensorCode(sensorCode), startTime, endTime).stream()
                .map(data -> new SensorDataResponse(
                        data.getId(),
                        data.getSensorCode(),
                        sensorNameMap.getOrDefault(data.getSensorCode(), data.getSensorCode()),
                        data.getTemperature(),
                        data.getHumidity(),
                        data.getCollectTime(),
                        data.getSourceFile()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlarmRecordResponse> queryAlarms(String sensorCode, AlarmType alarmType, AlarmStatus status,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, String> sensorNameMap = sensorInfoRepository.findAll().stream()
                .collect(Collectors.toMap(SensorInfo::getSensorCode, SensorInfo::getSensorName));
        return alarmRecordRepository.queryAlarms(normalizeSensorCode(sensorCode), alarmType, status, startTime, endTime)
                .stream()
                .map(alarm -> new AlarmRecordResponse(
                        alarm.getId(),
                        alarm.getSensorCode(),
                        sensorNameMap.getOrDefault(alarm.getSensorCode(), alarm.getSensorCode()),
                        alarm.getAlarmType(),
                        alarm.getCurrentValue(),
                        alarm.getThresholdDesc(),
                        alarm.getAlarmLevel(),
                        alarm.getStatus(),
                        alarm.getAlarmTime(),
                        alarm.getResolvedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        return new DashboardSummaryResponse(
                sensorInfoRepository.count(),
                sensorDataRepository.count(),
                alarmRecordRepository.countByStatus(AlarmStatus.ACTIVE),
                alarmRecordRepository.countByStatus(AlarmStatus.RESOLVED),
                sensorDataRepository.findLatestCollectTime()
        );
    }

    private void resolveAllActiveAlarms(String sensorCode, LocalDateTime resolvedAt) {
        List<AlarmRecord> activeAlarms = alarmRecordRepository.findBySensorCodeAndStatus(sensorCode, AlarmStatus.ACTIVE);
        for (AlarmRecord activeAlarm : activeAlarms) {
            activeAlarm.setStatus(AlarmStatus.RESOLVED);
            activeAlarm.setResolvedAt(resolvedAt);
            alarmRecordRepository.save(activeAlarm);
        }
    }

    private AlarmLevel resolveLevel(AlarmType alarmType, BigDecimal currentValue, AlarmRule rule) {
        BigDecimal deviation = switch (alarmType) {
            case TEMP_HIGH -> currentValue.subtract(rule.getTempMax());
            case TEMP_LOW -> rule.getTempMin().subtract(currentValue);
            case HUM_HIGH -> currentValue.subtract(rule.getHumidityMax());
            case HUM_LOW -> rule.getHumidityMin().subtract(currentValue);
        };

        BigDecimal baseline = switch (alarmType) {
            case TEMP_HIGH -> rule.getTempMax();
            case TEMP_LOW -> rule.getTempMin();
            case HUM_HIGH -> rule.getHumidityMax();
            case HUM_LOW -> rule.getHumidityMin();
        };

        BigDecimal ratio = deviation.divide(baseline.max(BigDecimal.ONE), 4, RoundingMode.HALF_UP);
        return ratio.compareTo(new BigDecimal("0.10")) >= 0 ? AlarmLevel.CRITICAL : AlarmLevel.WARNING;
    }

    private String thresholdText(AlarmType alarmType, AlarmRule rule) {
        return switch (alarmType) {
            case TEMP_HIGH -> "温度高于 " + rule.getTempMax() + "℃";
            case TEMP_LOW -> "温度低于 " + rule.getTempMin() + "℃";
            case HUM_HIGH -> "湿度高于 " + rule.getHumidityMax() + "%";
            case HUM_LOW -> "湿度低于 " + rule.getHumidityMin() + "%";
        };
    }

    private String normalizeSensorCode(String sensorCode) {
        return sensorCode == null || sensorCode.isBlank() ? null : sensorCode.trim().toUpperCase();
    }
}
