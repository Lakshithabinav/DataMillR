package com.example.modbusapplication.Repository;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.modbusapplication.Entity.UserInformation;

public interface UserRepository extends JpaRepository<UserInformation, Integer>{

    @Query("SELECT MAX(u.userKey) FROM UserInformation u")
    Integer findMaxUserKey();

    Optional<UserInformation> findByUserId(String userId);

   @Query(value = "SELECT user_key, company_name FROM user_information", nativeQuery = true)
List<Object[]> queryPartialUserInformation();


}
