package com.example.sample.sample.repository;

import com.example.sample.sample.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);
    Account findByAccountNumber(String accountNumber);

}
