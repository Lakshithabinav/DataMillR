package com.example.modbusapplication.Model;

import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModbusEntityDao {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private LocalDateTime endDateTime;
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
