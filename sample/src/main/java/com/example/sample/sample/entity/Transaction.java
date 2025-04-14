package com.example.sample.sample.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private LocalDateTime date = LocalDateTime.now();

    private String type; // "DEPOSIT" or "WITHDRAWAL"

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BankAccount account;
}

