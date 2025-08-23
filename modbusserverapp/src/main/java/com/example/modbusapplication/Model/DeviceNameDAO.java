package com.example.modbusapplication.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class DeviceNameDAO {
    private String deviceName;
    private Short deviceIds;
    private String machineName;
    private Double totalWeight;
    private Double setWeight;
    private String batchName;
}
