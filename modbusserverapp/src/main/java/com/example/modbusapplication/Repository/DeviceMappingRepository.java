<<<<<<< HEAD
package com.example.modbusapplication.Repository;

import com.example.modbusapplication.Entity.DeviceMapping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceMappingRepository extends JpaRepository<DeviceMapping, Short> {
      Optional<DeviceMapping> findByDeviceId(short deviceId);
}
=======
package com.example.modbusapplication.Repository;

import com.example.modbusapplication.Entity.DeviceMapping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceMappingRepository extends JpaRepository<DeviceMapping, Short> {
      Optional<DeviceMapping> findByDeviceId(short deviceId);
      List<DeviceMapping> findByUserKey(short userKey);

}
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
