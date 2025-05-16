package com.example.modbusapplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.modbusapplication.Entity.LoginInformation;

public interface LoginSessionRepository extends JpaRepository<LoginInformation, Long>{
    Optional<LoginInformation> findByIpAddress(String ipAddress);

}
