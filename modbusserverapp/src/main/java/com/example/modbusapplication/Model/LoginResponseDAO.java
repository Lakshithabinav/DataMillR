package com.example.modbusapplication.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDAO {
    private String userId;
    private int userKey;
    private String companyName;
    private List<DeviceNameDAO> devices;
}
