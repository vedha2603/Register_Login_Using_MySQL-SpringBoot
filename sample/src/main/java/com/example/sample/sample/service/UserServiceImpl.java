package com.example.sample.sample.service;

import com.example.sample.sample.model.User;
import com.example.sample.sample.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, User> pendingUsers = new ConcurrentHashMap<>();

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for Verification");
        message.setText(
            "Hello,\n\n" +
            "Your OTP for verification is:\n\n" +
            otp + "\n\n" +
            "This OTP is valid for one-time use only.\n\n" +
            "Regards,\nSample App Team"
        );

        mailSender.send(message);
    }

    @Override
    public void storePendingUser(User user) {
        System.out.println("Storing OTP for email: " + user.getEmail() + " | OTP: " + user.getOtp());
        pendingUsers.put(user.getEmail().toLowerCase(), user);
    }

    @Override
    public User getPendingUser(String email) {
        System.out.println("Retrieving OTP for email: " + email);
        return pendingUsers.get(email.toLowerCase());
    }

    @Override
    public void removePendingUser(String email) {
        pendingUsers.remove(email.toLowerCase());
    }
}
