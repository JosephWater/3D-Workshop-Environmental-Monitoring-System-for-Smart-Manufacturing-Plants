package com.sdu.factorymonitor.entity;

import com.sdu.factorymonitor.enums.AlarmLevel;
import com.sdu.factorymonitor.enums.AlarmStatus;
import com.sdu.factorymonitor.enums.AlarmType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alarm_record")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String sensorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmType alarmType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentValue;

    @Column(nullable = false, length = 120)
    private String thresholdDesc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmLevel alarmLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmStatus status;

    @Column(nullable = false)
    private LocalDateTime alarmTime;

    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        if (alarmTime == null) {
            alarmTime = LocalDateTime.now();
        }
    }
}
