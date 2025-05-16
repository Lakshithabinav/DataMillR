package com.example.modbusapplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class UserInformation {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int userKey;
    private String userId;
    private String password; 
    private String companyName;

    public UserInformation() {
    }

    public UserInformation(int userKey, String userId, String password, String companyName) {
    this.userKey = userKey;
    this.userId = userId;
    this.password = password;
    this.companyName = companyName;
}

    public Long getId() {
        return id;
    }

     public void setId(Long id) {
         this.id = id;
     }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public int getUserkey() {
        return userKey;
    }
    public void setUserkey(int userkey) {
        this.userKey = userkey;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }





}
