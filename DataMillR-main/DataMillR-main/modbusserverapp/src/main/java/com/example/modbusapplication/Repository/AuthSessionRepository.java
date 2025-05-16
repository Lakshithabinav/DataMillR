package com.example.modbusapplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.modbusapplication.Entity.loginInformation;

public interface AuthSessionRepository extends JpaRepository<loginInformation, Long>{
    Optional<loginInformation> findByIpAddress(String ipAddress);

}
