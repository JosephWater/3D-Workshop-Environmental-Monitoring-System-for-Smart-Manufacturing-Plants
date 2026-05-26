package com.sdu.factorymonitor.repository;

import com.sdu.factorymonitor.entity.SensorInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorInfoRepository extends JpaRepository<SensorInfo, Long> {

    Optional<SensorInfo> findBySensorCode(String sensorCode);
}
