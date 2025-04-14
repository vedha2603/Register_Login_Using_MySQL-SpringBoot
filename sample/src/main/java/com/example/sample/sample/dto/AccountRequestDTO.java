package com.example.sample.sample.dto;

import com.example.sample.sample.entity.AccountType; // ✅ Add this import
import lombok.Data;

@Data
public class AccountRequestDTO {
    private AccountType accountType;
}
