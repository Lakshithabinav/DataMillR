package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.modbusapplication.Model.RegDeviceDAO;
import com.example.modbusapplication.Model.SearchCompanyDao;
import com.example.modbusapplication.Service.AdminLogicService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;


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
        System.out.println("");
        return adminLogicService.registerdevice(regDeviceDAO);

    }
    @GetMapping("/search-company-name")
    public ResponseEntity<?> getMethodName(@RequestParam String companyName) {
        List<SearchCompanyDao> searchedCompanyName = adminLogicService.searchCompanyName(companyName);
        return ResponseEntity.status(HttpStatus.OK).body(searchedCompanyName);

    }
    
    

}
