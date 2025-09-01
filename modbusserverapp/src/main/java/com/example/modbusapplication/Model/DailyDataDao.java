package com.example.modbusapplication.Model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Data
@Setter
public class DailyDataDao {
    private String DeviceID;
    private int sumOfTotalWeight;
}
