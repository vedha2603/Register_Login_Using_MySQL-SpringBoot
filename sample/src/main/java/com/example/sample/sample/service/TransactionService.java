package com.example.sample.sample.service;

import com.example.sample.sample.model.Transaction;
import com.example.sample.sample.repository.TransactionRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
    }

    public void saveTransaction(String accountNumber, String type, double amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
    
}
