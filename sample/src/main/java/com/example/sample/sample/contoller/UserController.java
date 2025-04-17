package com.example.sample.sample.contoller;

import com.example.sample.sample.model.User;
import com.example.sample.sample.model.Account;
import com.example.sample.sample.repository.AccountRepository;
import com.example.sample.sample.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$";
        return Pattern.matches(pattern, password);
    }

    // ------------------- REGISTER -------------------

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
            model.addAttribute("message", "Invalid OTP.");
            return "verify-registration-otp";
        }

        pendingUser.setOtp(null);
        userService.saveUser(pendingUser);
        userService.removePendingUser(email);

        model.addAttribute("message", "Registration successful. Please login.");
        return "login";
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

        if (existingUser == null) {
            model.addAttribute("message", "Invalid email or password!");
            return "login";
        }

        if (existingUser.getAccountLockedUntil() != null &&
                existingUser.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            model.addAttribute("message", "Account is locked. Try again after: " + existingUser.getAccountLockedUntil());
            return "login";
        }

        if (!existingUser.getPassword().equals(user.getPassword())) {
            int attempts = existingUser.getFailedLoginAttempts() + 1;
            existingUser.setFailedLoginAttempts(attempts);

            if (attempts >= 3) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(3);
                existingUser.setAccountLockedUntil(lockUntil);
                userService.saveUser(existingUser);

                try {
                    userService.sendAccountLockedEmail(existingUser.getEmail(), lockUntil);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                model.addAttribute("message", "Account locked due to 3 failed attempts. Try again after: " + lockUntil);
                return "login";
            }

            userService.saveUser(existingUser);
            model.addAttribute("message", "Invalid credentials! Attempt " + attempts + " of 3.");
            return "login";
        }

        existingUser.setFailedLoginAttempts(0);
        existingUser.setAccountLockedUntil(null);
        userService.saveUser(existingUser);

        return "redirect:/dashboard?email=" + existingUser.getEmail();
    }

    // ------------------- DASHBOARD -------------------

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("message", "User not found.");
            return "login";
        }

        model.addAttribute("user", user);
        model.addAttribute("email", user.getEmail());
        return "dashboard";
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

    // ------------------- CREATE ACCOUNT -------------------

    @GetMapping("/create-account")
    public String showCreateAccountForm() {
        return "create-account";
    }

    @PostMapping("/create-account")
    public String createAccount(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String mobileNumber,
                                @RequestParam String dob,
                                @RequestParam String accountType,
                                Model model) {

        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("message", "No registered user found with this email. Please register first.");
            return "create-account";
        }

        if (accountRepository.findByEmail(email) != null) {
            model.addAttribute("message", "Account already exists for this email.");
            return "create-account";
        }

        LocalDate dateOfBirth = LocalDate.parse(dob);
        LocalDate today = LocalDate.now();
        int age = today.getYear() - dateOfBirth.getYear();
        if (dateOfBirth.plusYears(age).isAfter(today)) age--;

        if (age < 18) {
            model.addAttribute("message", "You must be at least 18 years old to create an account.");
            return "create-account";
        }

        String accountNumber = "ACC" + String.format("%010d", new Random().nextInt(1_000_000_000));
        Account account = new Account();
        account.setName(name);
        account.setEmail(email);
        account.setMobileNumber(mobileNumber);
        account.setDob(dateOfBirth);
        account.setAccountType(accountType);
        account.setAccountNumber(accountNumber);
        account.setBalance(0.0);

        accountRepository.save(account);

        model.addAttribute("accountNumber", accountNumber);
        return "success";
    }

    // ------------------- VIEW ACCOUNT -------------------

    @PostMapping("/view-account")
    public String viewAccountDetails(@RequestParam("email") String email, Model model) {
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("message", "Email is required to view account details.");
            return "dashboard";
        }

        String jpql = "SELECT u, a FROM User u LEFT JOIN Account a ON u.email = a.email WHERE u.email = :email";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("email", email);

        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            model.addAttribute("message", "User not found.");
            return "dashboard";
        }

        Object[] result = results.get(0);
        User user = (User) result[0];
        Account account = (Account) result[1];

        if (account == null) {
            model.addAttribute("message", "No account exists for this user.");
        }

        model.addAttribute("user", user);
        model.addAttribute("account", account);
        return "view-account";
    }

    // ------------------- BANK TRANSFER -------------------

    @GetMapping("/banktransfer")
    public String showTransactionForm() {
        return "banktransfer";
    }

    @PostMapping("/banktransfer")
    public String processTransaction(@RequestParam String senderAccountNumber,
                                     @RequestParam String receiverAccountNumber,
                                     @RequestParam double amount,
                                     Model model) {

        Account senderAccount = accountRepository.findByAccountNumber(senderAccountNumber);
        Account receiverAccount = accountRepository.findByAccountNumber(receiverAccountNumber);

        if (receiverAccount == null) {
            model.addAttribute("message", "Receiver account not found.");
            return "banktransfer";
        }

        if (senderAccount == null) {
            model.addAttribute("message", "Sender account not found.");
            return "banktransfer";
        }

        if (amount <= 0) {
            model.addAttribute("message", "Amount must be greater than 0.");
            return "banktransfer";
        }

        if (senderAccount.getBalance() < amount) {
            model.addAttribute("message", "Insufficient balance.");
            return "banktransfer";
        }

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + amount);

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        model.addAttribute("message", "Transaction successful!");
        model.addAttribute("senderBalance", senderAccount.getBalance());
        return "banktransfer";
    }

    // ------------------- DEPOSIT / WITHDRAW -------------------

    @GetMapping("/DepoWithdraw")
    public String showDepositWithdrawForm() {
        return "DepoWithdraw";
    }

    @PostMapping("/DepoWithdraw")
    public String processDepositWithdraw(@RequestParam String accountNumber,
                                         @RequestParam String transactionType,
                                         @RequestParam double amount,
                                         Model model) {

        Account account = accountRepository.findByAccountNumber(accountNumber);

        if (account == null) {
            model.addAttribute("error", "Account not found.");
            return "DepoWithdraw";
        }

        if (amount <= 0) {
            model.addAttribute("error", "Amount must be greater than 0.");
            return "DepoWithdraw";
        }

        if ("deposit".equals(transactionType)) {
            account.setBalance(account.getBalance() + amount);
        } else if ("withdraw".equals(transactionType)) {
            if (account.getBalance() < amount) {
                model.addAttribute("error", "Insufficient balance.");
                return "DepoWithdraw";
            }
            account.setBalance(account.getBalance() - amount);
        } else {
            model.addAttribute("error", "Invalid transaction type.");
            return "DepoWithdraw";
        }

        accountRepository.save(account);
        model.addAttribute("message", "Transaction successful! New Balance: " + account.getBalance());
        return "DepoWithdraw";
    }
}
