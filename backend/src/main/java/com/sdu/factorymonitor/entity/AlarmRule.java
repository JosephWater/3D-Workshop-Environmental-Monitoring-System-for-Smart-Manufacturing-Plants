package com.sdu.factorymonitor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alarm_rule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tempMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tempMax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal humidityMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal humidityMax;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        updatedAt = LocalDateTime.now();
    }
}
