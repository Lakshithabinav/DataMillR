package com.example.modbusapplication.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Service.UserService;
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

@PostMapping("/data")
public ResponseEntity<?> getModbusData(@RequestBody ModbusDataRequestDAO requestDAO) {
    try {
        Object response = userService.fetchModbusDataFlexible(requestDAO);
        return ResponseEntity.ok(response); // Will return either:
                                            // - List<ModbusEntityDao>
                                            // - ModbusGroupedBatchResponse
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", "Something went wrong"));
    }
}





}
