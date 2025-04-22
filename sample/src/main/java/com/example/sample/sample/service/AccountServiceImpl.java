package com.example.sample.sample.service;

import com.example.sample.sample.model.Account;
import com.example.sample.sample.repository.AccountRepository;
import com.example.sample.sample.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    // Constructor injection of AccountRepository
    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account getAccountByEmail(String email) {
        // Fetch the account by email
        return accountRepository.findByEmail(email);
    }
}
