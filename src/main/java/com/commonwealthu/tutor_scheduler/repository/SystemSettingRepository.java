package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {

}