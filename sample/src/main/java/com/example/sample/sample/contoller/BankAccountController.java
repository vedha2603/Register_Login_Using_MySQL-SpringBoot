package com.example.sample.sample.contoller;
import com.example.sample.sample.dto.AccountRequestDTO;
import com.example.sample.sample.dto.TransactionResponseDTO;
import com.example.sample.sample.entity.BankAccount;
import com.example.sample.sample.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService accountService;

    @PostMapping
    public BankAccount createAccount(@RequestBody AccountRequestDTO dto) {
        return accountService.createAccount(dto);
    }

    @GetMapping("/{id}")
    public BankAccount getAccount(@PathVariable Long id) {
        return accountService.getAccountDetails(id);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponseDTO> getTransactions(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String type
    ) {
        return accountService.getTransactions(id, from, to, type);
    }
}
