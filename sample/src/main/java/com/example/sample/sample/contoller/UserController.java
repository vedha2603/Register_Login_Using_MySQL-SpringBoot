package com.example.sample.sample.contoller;

import com.example.sample.sample.model.User;
import com.example.sample.sample.model.Account;
import com.example.sample.sample.model.Transaction;
import com.example.sample.sample.repository.AccountRepository;
import com.example.sample.sample.repository.TransactionRepository;
import com.example.sample.sample.service.AccountService;
import com.example.sample.sample.service.TransactionService;
import com.example.sample.sample.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Random;

@Controller
public class UserController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

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
                            @RequestParam String password,
                            @RequestParam String confirmPassword, // Added confirm password check
                            Model model) {

    // Check if the email already exists for an account
    if (accountRepository.findByEmail(email) != null) {
        model.addAttribute("message", "Account already exists for this email.");
        return "create-account";
    }

    // Validate password and confirm password match
    if (!password.equals(confirmPassword)) {
        model.addAttribute("message", "Passwords do not match.");
        return "create-account";
    }

    // Validate password strength
    if (!isValidPassword(password)) {
        model.addAttribute("message", "Password must be at least 6 characters long, contain one uppercase letter, one lowercase letter, one digit, and one special character.");
        return "create-account";
    }

    // Check if the user already exists
    if (userService.findByEmail(email) == null) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setName(name);
        userService.saveUser(newUser);
    }

    // Validate age (must be at least 18)
    LocalDate dateOfBirth = LocalDate.parse(dob);
    LocalDate today = LocalDate.now();
    int age = today.getYear() - dateOfBirth.getYear();
    if (dateOfBirth.plusYears(age).isAfter(today)) age--;

    if (age < 18) {
        model.addAttribute("message", "You must be at least 18 years old to create an account.");
        return "create-account";
    }

    // Generate a random account number
    String accountNumber = "ACC" + String.format("%010d", new Random().nextInt(1_000_000_000));
    
    // Create and save the new account
    Account account = new Account();
    account.setName(name);
    account.setEmail(email);
    account.setMobileNumber(mobileNumber);
    account.setDob(dateOfBirth);
    account.setAccountType(accountType);
    account.setAccountNumber(accountNumber);
    account.setBalance(0.0);

    accountRepository.save(account);

    // Redirect to the success page with the account number
    return "redirect:/success?accountNumber=" + accountNumber;
}

// Password validation function
private boolean isValidPassword(String password) {
    String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,}$";
    return password != null && password.matches(pattern);
}



    // ------------------- SUCCESS PAGE -------------------

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam String accountNumber, Model model) {
        model.addAttribute("accountNumber", accountNumber);
        return "success";
    }



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

        if (accountRepository.findByEmail(user.getEmail()) == null) {
            model.addAttribute("message", "No account exists for this user. Please create an account first.");
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

    @PostMapping("/view-account")
    public String viewAccountDetails(@RequestParam("email") String email, Model model) {
        String jpql = "SELECT u, a FROM User u LEFT JOIN Account a ON u.email = a.email WHERE u.email = :email";
        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                                              .setParameter("email", email)
                                              .getResultList();

        if (results.isEmpty()) {
            model.addAttribute("message", "User not found.");
            return "dashboard";
        }

        Object[] result = results.get(0);
        User user = (User) result[0];
        Account account = (Account) result[1];

        model.addAttribute("user", user);
        model.addAttribute("account", account);
        return "view-account";
    }

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

    if (receiverAccount == null || senderAccount == null) {
        model.addAttribute("message", "Sender or Receiver account not found.");
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

    // Save transaction records for both sender and receiver
    transactionService.saveTransaction(senderAccountNumber, "transfer", amount, "Transferred to " + receiverAccountNumber);
    transactionService.saveTransaction(receiverAccountNumber, "transfer", amount, "Received from " + senderAccountNumber);

    model.addAttribute("message", "Transaction successful!");
    model.addAttribute("senderBalance", senderAccount.getBalance());
    return "banktransfer";
}

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
        transactionService.saveTransaction(accountNumber, "deposit", amount, "Deposited into account");
    } else if ("withdraw".equals(transactionType)) {
        if (account.getBalance() < amount) {
            model.addAttribute("error", "Insufficient balance.");
            return "DepoWithdraw";
        }
        account.setBalance(account.getBalance() - amount);
        transactionService.saveTransaction(accountNumber, "withdraw", amount, "Withdrawn from account");
    } else {
        model.addAttribute("error", "Invalid transaction type.");
        return "DepoWithdraw";
    }

    accountRepository.save(account);
    model.addAttribute("message", "Transaction successful! New Balance: " + account.getBalance());
    return "DepoWithdraw";
}

@GetMapping("/transaction-history")
public String showTransactionHistoryPage(@RequestParam(value = "accountNumber", required = false) String accountNumber,
                                         Model model) {
    if (accountNumber != null && !accountNumber.isEmpty()) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        model.addAttribute("transactions", transactions);
    }
    return "transaction-history";
}


    
}