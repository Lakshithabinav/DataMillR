package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Service.AdminLogicService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminLogicService adminLogicService;

    @PostMapping("/create-table")
    public ResponseEntity<?> createTable(@RequestParam String deviceId) {
        System.out.println("deviceId ==" +deviceId);
        return adminLogicService.createNewTable(deviceId);
    }

    @PostMapping("/register-device")
    public ResponseEntity<Map<String, String>> registerDevice(@RequestBody Map<String, String> request){
        String deviceId = request.get("deviceId");
        String deviceName = request.get("deviceName");
        String companyName = request.get("companyName");

       Map<String, String> result = adminLogicService.registerdevice(deviceId, deviceName, companyName);
        result.put("deviceId", deviceId);
        result.put("deviceName", deviceName);
        result.put("companyName", companyName);

    return ResponseEntity.ok(result);

    }

  @PostMapping("/map-device-to-user")
    public ResponseEntity<?> mapDeviceToUser(@RequestBody Map<String, String> request) {
        String deviceId = request.get("deviceId");
        String companyName = request.get("companyName");
        return adminLogicService.mapDeviceToExistUser(deviceId, companyName);
    }

}
