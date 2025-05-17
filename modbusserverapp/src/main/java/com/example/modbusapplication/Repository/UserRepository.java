package com.example.modbusapplication.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.modbusapplication.Entity.UserInformation;

public interface UserRepository extends JpaRepository<UserInformation, Integer>{
    @Query("SELECT MAX(u.userKey) FROM UserInformation u")
    Integer findMaxUserKey();
    Optional<UserInformation> findByUserId(String userId);

}
