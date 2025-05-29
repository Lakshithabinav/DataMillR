package com.example.modbusapplication.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeviceNameDAO {
    private List<String> deviceName;
    private List<Short> deviceIds;
}
