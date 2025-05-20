package com.example.modbusapplication.Entity;

import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class UserInformation {
     @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;
    private Integer userKey;
    private String userId;
    private String loginPassword; 
    private String companyName;

    public UserInformation() {
    }

    public UserInformation(int userKey, String userId, String loginPassword, String companyName) {
    this.userKey = userKey;
    this.userId = userId;
    this.loginPassword = loginPassword;
    this.companyName = companyName;
}

    // public Long getId() {
    //     return id;
    // }

    //  public void setId(Long id) {
    //      this.id = id;
    //  }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
<<<<<<< HEAD
    public int getUserkey() {
        return userKey;
    }
    public void setUserkey(int userkey) {
        this.userKey = userkey;
=======
    public int getUserKey() {
        return userKey;
    }
    public void setUserKey(int userKey) {
        this.userKey = userKey;
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return loginPassword;
    }
    public void setPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

<<<<<<< HEAD




}
=======
}
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
