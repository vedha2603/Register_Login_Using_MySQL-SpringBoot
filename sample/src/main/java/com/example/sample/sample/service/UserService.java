package com.example.sample.sample.service;

import com.example.sample.sample.model.User;

import jakarta.mail.MessagingException;

public interface UserService {
    User findByEmail(String email);
    void saveUser(User user);
    void sendOtpEmail(String to, String otp) throws MessagingException;

    // For OTP verification during registration
    void storePendingUser(User user);
    User getPendingUser(String email);
    void removePendingUser(String email);
}
