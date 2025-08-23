package com.example.modbusapplication.Model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusDataRequestDAO {
    private Short deviceId;
    private String startDate; 
    private String endDate;
    private Integer month;
    private Integer year;
    private Boolean allYearHistory;   
}
