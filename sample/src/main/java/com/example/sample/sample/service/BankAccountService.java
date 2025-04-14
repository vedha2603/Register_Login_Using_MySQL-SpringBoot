package com.example.sample.sample.service;

import com.example.sample.sample.dto.AccountRequestDTO;
import com.example.sample.sample.dto.TransactionResponseDTO;
import com.example.sample.sample.entity.BankAccount;
import com.example.sample.sample.entity.Transaction;
import com.example.sample.sample.repository.BankAccountRepository;
import com.example.sample.sample.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository accountRepo;
    private final TransactionRepository transactionRepo; // ✅ Inject transactionRepo

    public BankAccount createAccount(AccountRequestDTO dto) {
        BankAccount account = new BankAccount();
        account.setAccountType(dto.getAccountType());
        account.setAccountNumber(UUID.randomUUID().toString().substring(0, 12));
        return accountRepo.save(account);
    }

    public BankAccount getAccountDetails(Long id) {
        return accountRepo.findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    // ✅ New method to support transaction filtering
    public List<TransactionResponseDTO> getTransactions(Long accountId, LocalDateTime from, LocalDateTime to, String type) {
        BankAccount account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Transaction> transactions;

        if (type != null && !type.isEmpty()) {
            transactions = transactionRepo.findByAccountAndDateBetweenAndType(account, from, to, type);
        } else {
            transactions = transactionRepo.findByAccountAndDateBetween(account, from, to);
        }

        return transactions.stream()
                .map(tx -> new TransactionResponseDTO(tx.getId(), tx.getAmount(), tx.getDate(), tx.getType()))
                .collect(Collectors.toList());
    }
}
