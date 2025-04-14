package com.example.sample.sample.repository;
import com.example.sample.sample.entity.Transaction;
import com.example.sample.sample.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAndDateBetween(BankAccount account, LocalDateTime from, LocalDateTime to);
    List<Transaction> findByAccountAndDateBetweenAndType(BankAccount account, LocalDateTime from, LocalDateTime to, String type);
    
}
