package com.sdu.factorymonitor.repository;

import com.sdu.factorymonitor.entity.AlarmRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRuleRepository extends JpaRepository<AlarmRule, Long> {
}
