package com.example.sample.sample.service;

import com.example.sample.sample.model.User;
import com.example.sample.sample.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        String subject = "Your OTP for Verification";
        String body = "Hello,\n\n" +
                      "Your OTP for verification is:\n\n" +
                      otp + "\n\n" +
                      "This OTP is valid for one-time use only.\n\n" +
                      "Regards,\nSample App Team";
        sendEmail(to, subject, body);
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

    @Override
    public void sendAccountLockedEmail(String toEmail, LocalDateTime unlockTime) {
        String subject = "Account Locked Due to Failed Login Attempts";
        String body = "Your account has been locked due to 3 failed login attempts.\n\n" +
                      "You can try again after: " + unlockTime + ".\n\n" +
                      "If this wasn't you, please secure your account immediately.";
        sendEmail(toEmail, subject, body);
    }

    // ✅ Core reusable method
    private void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("vedhadharshinij2003@gmail.com"); // Use your verified sender email
        mailSender.send(message);
    }
}
