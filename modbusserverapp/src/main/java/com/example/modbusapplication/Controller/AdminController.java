package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Model.RegDeviceDAO;
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
        System.out.println("deviceId ==" + deviceId);
        return adminLogicService.createNewTable(deviceId);
    }

    @PostMapping("/register-device")
    public ResponseEntity<?> registerDevice(@RequestBody RegDeviceDAO regDeviceDAO) {
        return adminLogicService.registerdevice(regDeviceDAO);

    }

}
