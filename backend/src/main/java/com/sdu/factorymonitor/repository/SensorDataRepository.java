package com.sdu.factorymonitor.repository;

import com.sdu.factorymonitor.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Optional<SensorData> findFirstBySensorCodeOrderByCollectTimeDescIdDesc(String sensorCode);

    @Query("""
            select s from SensorData s
            where (:sensorCode is null or s.sensorCode = :sensorCode)
              and (:startTime is null or s.collectTime >= :startTime)
              and (:endTime is null or s.collectTime <= :endTime)
            order by s.collectTime desc, s.id desc
            """)
    List<SensorData> queryHistory(@Param("sensorCode") String sensorCode,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    @Query("select max(s.collectTime) from SensorData s")
    LocalDateTime findLatestCollectTime();
}
