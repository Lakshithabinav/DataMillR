package com.example.modbusapplication.Model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusEntityDao {

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
