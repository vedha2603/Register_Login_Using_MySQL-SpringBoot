package com.example.sample.sample.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "otp")
    private String otp;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    // ---- Constructors ----
    public User() {}

    public User(Long id, String name, String email, String password, String otp, int failedLoginAttempts, LocalDateTime accountLockedUntil) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.otp = otp;
        this.failedLoginAttempts = failedLoginAttempts;
        this.accountLockedUntil = accountLockedUntil;
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

}
