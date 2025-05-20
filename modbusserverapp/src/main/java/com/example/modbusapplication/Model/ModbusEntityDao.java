<<<<<<< HEAD
package com.example.modbusapplication.Model;

import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusEntityDao {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private String batchName;

    private int setWeight;

    private int actualWeight;

    private int totalWeight;

    private Short deviceId;

    public ModbusEntityDao() {
    }

    public ModbusEntityDao(LocalDateTime timestamp, String batchName, int setWeight, int actualWeight, int totalWeight,
            Short deviceId) {
        this.timestamp = timestamp;
        this.batchName = batchName;
        this.setWeight = setWeight;
        this.actualWeight = actualWeight;
        this.totalWeight = totalWeight;
        this.deviceId = deviceId;
    }

}
=======
package com.example.modbusapplication.Model;

import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusEntityDao {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private String batchName;

    private int setWeight;

    private int actualWeight;

    private int totalWeight;

    private Short deviceId;

    public ModbusEntityDao() {
    }

    public ModbusEntityDao(LocalDateTime timestamp, String batchName, int setWeight, int actualWeight, int totalWeight,
            Short deviceId) {
        this.timestamp = timestamp;
        this.batchName = batchName;
        this.setWeight = setWeight;
        this.actualWeight = actualWeight;
        this.totalWeight = totalWeight;
        this.deviceId = deviceId;
    }

}
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
