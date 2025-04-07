package com.example.sample.sample.contoller;

import com.example.sample.sample.model.User;
import com.example.sample.sample.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.regex.Pattern;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$";
        return Pattern.matches(pattern, password);
    }

    // ------------------- REGISTER PHASE -------------------

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String startRegistration(@ModelAttribute("user") User user,
                                    @RequestParam("confirmPassword") String confirmPassword,
                                    Model model) {

        if (userService.findByEmail(user.getEmail()) != null) {
            model.addAttribute("message", "Email already exists!");
            return "register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("message", "Passwords do not match!");
            return "register";
        }

        if (!isValidPassword(user.getPassword())) {
            model.addAttribute("message", "Password must be at least 8 characters long and contain at least one uppercase, lowercase, digit, and special character.");
            return "register";
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        userService.saveUser(user);

        try {
            userService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to send email.");
            return "verify-registration-otp";
        }

        model.addAttribute("email", user.getEmail());
        return "verify-registration-otp";
    }

    @PostMapping("/verify-registration-otp")
    public String verifyRegistrationOtp(@RequestParam("email") String email,
                                        @RequestParam("otp") String otp,
                                        Model model) {
        User pendingUser = userService.findByEmail(email);
        if (pendingUser == null || !otp.equals(pendingUser.getOtp())) {
            model.addAttribute("message", "Registration successful. Please login.");
            return "login";
        }

        // OTP is valid, clear it and activate user (if needed)
        pendingUser.setOtp(null);
        userService.saveUser(pendingUser);
        userService.removePendingUser(email); // If applicable
        return "verify-registration-otp";
       
    }

    // ------------------- LOGIN -------------------

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") User user, Model model) {
        User existingUser = userService.findByEmail(user.getEmail());
        if (existingUser == null || !existingUser.getPassword().equals(user.getPassword())) {
            model.addAttribute("message", "Invalid email or password!");
            return "login";
        }
        model.addAttribute("user", existingUser);
        return "success";
    }

    // ------------------- FORGOT PASSWORD -------------------

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("message", "Email not found.");
            return "forgot-password";
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        userService.saveUser(user);

        try {
            userService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to send email.");
            return "forgot-password";
        }

        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyForgotPasswordOtp(@RequestParam("email") String email,
                                          @RequestParam("otp") String otp,
                                          Model model) {
        User user = userService.findByEmail(email);
        if (user == null || !user.getOtp().equals(otp)) {
            model.addAttribute("message", "Invalid OTP.");
            return "verify-otp";
        }

        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "Passwords do not match!");
            return "reset-password";
        }

        if (!isValidPassword(newPassword)) {
            model.addAttribute("message", "Password must be strong (8+ chars, upper, lower, digit, special).");
            return "reset-password";
        }

        User user = userService.findByEmail(email);
        user.setPassword(newPassword);
        user.setOtp(null);
        userService.saveUser(user);

        model.addAttribute("message", "Password reset successful.");
        return "login";
    }
}
