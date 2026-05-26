package com.sdu.factorymonitor.repository;

import com.sdu.factorymonitor.entity.AlarmRecord;
import com.sdu.factorymonitor.enums.AlarmStatus;
import com.sdu.factorymonitor.enums.AlarmType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRecordRepository extends JpaRepository<AlarmRecord, Long> {

    List<AlarmRecord> findBySensorCodeAndStatus(String sensorCode, AlarmStatus status);

    Optional<AlarmRecord> findBySensorCodeAndAlarmTypeAndStatus(String sensorCode, AlarmType alarmType, AlarmStatus status);

    long countByStatus(AlarmStatus status);

    @Query("""
            select a from AlarmRecord a
            where (:sensorCode is null or a.sensorCode = :sensorCode)
              and (:alarmType is null or a.alarmType = :alarmType)
              and (:status is null or a.status = :status)
              and (:startTime is null or a.alarmTime >= :startTime)
              and (:endTime is null or a.alarmTime <= :endTime)
            order by a.alarmTime desc, a.id desc
            """)
    List<AlarmRecord> queryAlarms(@Param("sensorCode") String sensorCode,
                                  @Param("alarmType") AlarmType alarmType,
                                  @Param("status") AlarmStatus status,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);
}
