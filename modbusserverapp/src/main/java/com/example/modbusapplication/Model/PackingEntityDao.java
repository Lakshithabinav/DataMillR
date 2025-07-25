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
public class PackingEntityDao {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String batchId;
    private int bagWeightSet;
    private int actualWeight;
    private String scaleUsed;
    private int bagCount;
    private int accumulatedWeight;
    private String machineStatus; // RUNNING or STOPPED
    private Short deviceId;
    private String machinetype = "Packing Machine";


    public PackingEntityDao() {
    }


        // New packing data constructor
    public PackingEntityDao(LocalDateTime timestamp, String batchId, int bagWeightSet, int actualWeight,
            String scaleUsed, int bagCount, int accumulatedWeight, String machineStatus, Short deviceId) {
        this.timestamp = timestamp;
        this.batchId = batchId;
        this.bagWeightSet = bagWeightSet;
        this.actualWeight = actualWeight;
        this.scaleUsed = scaleUsed;
        this.bagCount = bagCount;
        this.accumulatedWeight = accumulatedWeight;
        this.machineStatus = machineStatus;
        this.deviceId = deviceId;

    }

}
