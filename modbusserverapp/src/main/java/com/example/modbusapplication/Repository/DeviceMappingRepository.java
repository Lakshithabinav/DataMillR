package com.example.modbusapplication.Repository;

import com.example.modbusapplication.Entity.DeviceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceMappingRepository extends JpaRepository<DeviceMapping, Long> {
}
