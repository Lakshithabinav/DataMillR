package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Service.AdminLogicService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

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
    public ResponseEntity<?> registerDevice(
        @RequestParam String deviceId,
        @RequestParam String deviceName,
        @RequestParam String companyName) {

    return adminLogicService.createNewTable(deviceId, deviceName, companyName);

}


}
