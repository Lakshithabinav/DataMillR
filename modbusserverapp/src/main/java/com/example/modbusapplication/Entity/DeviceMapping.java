<<<<<<< HEAD
package com.example.modbusapplication.Entity;

import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_mapping")
@Getter
@Setter
public class DeviceMapping {

    public DeviceMapping(short deviceId) {
        this.deviceId =deviceId;
    }

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    private short userKey;

    private short deviceId;

    private String deviceName;

    public DeviceMapping(short deviceId, String deviceName, short userKey) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userKey = userKey;
    }
public DeviceMapping() {
}


}
=======
package com.example.modbusapplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_mapping")
@Getter
@Setter
public class DeviceMapping {

    public DeviceMapping(short deviceId) {
        this.deviceId =deviceId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private short userKey;

    private short deviceId;

    private String deviceName;

    public DeviceMapping(short deviceId, String deviceName, short userKey) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userKey = userKey;
    }
public DeviceMapping() {
}


}
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
