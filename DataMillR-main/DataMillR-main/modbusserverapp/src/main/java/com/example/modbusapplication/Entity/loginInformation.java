package com.example.modbusapplication.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class loginInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    private String randomNumber;
    private LocalDateTime hitTime;
    
    public Long getId() {
        return id;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public String getRandomNumber() {
        return randomNumber;
    }
    public LocalDateTime getHitTime() {
        return hitTime;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setRandomNumber(String randomNumber) {
        this.randomNumber = randomNumber;
    }
    public void setHitTime(LocalDateTime hitTime) {
        this.hitTime = hitTime;
    }
    
}
