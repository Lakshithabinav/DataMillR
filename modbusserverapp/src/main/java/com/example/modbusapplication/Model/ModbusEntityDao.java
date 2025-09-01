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

    // Old structure (legacy)
    private int status;
    private String batchName;
    private int flowrate;
    private int setWeight;
    private int presentWeight;
    private int totalWeight;
    private int noOfTotalWeight = 0;
    private Short deviceId;
    private boolean isEndOfBatch;
    private String machinetype = "Flow Veyor";



    public ModbusEntityDao() {
    }

    // Old structure constructor
    public ModbusEntityDao(LocalDateTime timestamp, int status, int flowrate, String batchName, int setWeight, int presentWeight, int totalWeight,
            Short deviceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.flowrate = flowrate;
        this.batchName = batchName;
        this.setWeight = setWeight;
        this.presentWeight = presentWeight;
        this.totalWeight = totalWeight;
        this.deviceId = deviceId;
     
    }


}

  