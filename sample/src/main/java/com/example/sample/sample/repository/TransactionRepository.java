package com.example.sample.sample.repository;

import com.example.sample.sample.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions where the user is either the sender or receiver
    List<Transaction> findBySenderAccountNumberOrReceiverAccountNumber(String senderAccountNumber, String receiverAccountNumber);
    List<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber);
    // Optional: Find all transactions by a specific account number
    List<Transaction> findBySenderAccountNumber(String accountNumber);
    List<Transaction> findByReceiverAccountNumber(String accountNumber);
    List<Transaction> findByAccountNumber(String accountNumber);
    
}
