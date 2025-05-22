package com.example.modbusapplication.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCompanyDao {
    private String companyName;
    private int userKey;

    // ✅ Required constructor for JPQL
    public SearchCompanyDao(String companyName, int userKey) {
        this.companyName = companyName;
        this.userKey = userKey;
    }
}
