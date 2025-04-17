package com.example.sample.sample.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String mobileNumber;
    private LocalDate dob;
    private String accountType;
    private String accountNumber;
    private String otp;
    private Double balance = 0.0;
    private LocalDateTime createdOn = LocalDateTime.now();



}
