<<<<<<< HEAD
package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Service.AdminLogicService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<?> registerDevice(
            @RequestParam String deviceId,
            @RequestParam String deviceName,
            @RequestParam String companyName) {
        return adminLogicService.createNewTable(deviceId, deviceName, companyName);

    }
    @GetMapping("/getcompany-details")
    public String postMethodName(@RequestBody String companyCompany) {
        return companyCompany;
        //TODO: process POST request
        
        
    }
    

}
=======
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
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
