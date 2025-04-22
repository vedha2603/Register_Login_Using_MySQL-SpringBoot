package com.example.sample.sample.service;


import com.example.sample.sample.model.Account;

public interface AccountService {
    // Method to get an account by email
    Account getAccountByEmail(String email);
}
