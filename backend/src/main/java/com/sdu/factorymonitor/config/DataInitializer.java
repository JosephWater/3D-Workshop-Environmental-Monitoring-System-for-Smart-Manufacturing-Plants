package com.sdu.factorymonitor.config;

import com.sdu.factorymonitor.entity.AlarmRule;
import com.sdu.factorymonitor.entity.SensorInfo;
import com.sdu.factorymonitor.repository.AlarmRuleRepository;
import com.sdu.factorymonitor.repository.SensorInfoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initData(SensorInfoRepository sensorInfoRepository,
                                      AlarmRuleRepository alarmRuleRepository) {
        return args -> {
            if (sensorInfoRepository.count() == 0) {
                sensorInfoRepository.saveAll(defaultSensors());
            }

            if (alarmRuleRepository.count() == 0) {
                alarmRuleRepository.save(AlarmRule.builder()
                        .tempMin(new BigDecimal("18"))
                        .tempMax(new BigDecimal("30"))
                        .humidityMin(new BigDecimal("40"))
                        .humidityMax(new BigDecimal("70"))
                        .enabled(true)
                        .build());
            }
        };
    }

    private List<SensorInfo> defaultSensors() {
        return List.of(
                sensor("S1", "一号传感器", "1-1", -12, 1, -12),
                sensor("S2", "二号传感器", "1-2", 0, 1, -12),
                sensor("S3", "三号传感器", "1-3", 12, 1, -12),
                sensor("S4", "四号传感器", "2-1", -12, 1, 0),
                sensor("S5", "五号传感器", "2-2", 0, 1, 0),
                sensor("S6", "六号传感器", "2-3", 12, 1, 0),
                sensor("S7", "七号传感器", "3-1", -12, 1, 12),
                sensor("S8", "八号传感器", "3-2", 0, 1, 12),
                sensor("S9", "九号传感器", "3-3", 12, 1, 12)
        );
    }

    private SensorInfo sensor(String code, String name, String grid, double x, double y, double z) {
        return SensorInfo.builder()
                .sensorCode(code)
                .sensorName(name)
                .gridPosition(grid)
                .xPos(x)
                .yPos(y)
                .zPos(z)
                .remark("九宫格点位 " + grid)
                .build();
    }
}
