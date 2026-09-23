package com.ewallet.backend.configuration;

import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DatabaseSeeder(UserRepository userRepository, 
                          WalletRepository walletRepository, 
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap-admin.email:}") String adminEmail,
                          @Value("${app.bootstrap-admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.warn("Bootstrap admin is not configured; skipping admin seeding.");
            return;
        }

        if (!userRepository.existsByEmailAndDeletedFalse(adminEmail)) {
            log.info("Seeding default admin user...");
            
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPhone("+84999999999");
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            admin.setUserStatus(UserStatus.ACTIVE);
            
            User savedAdmin = userRepository.save(admin);
            
            Wallet wallet = Wallet.builder()
                    .user(savedAdmin)
                    .balance(BigDecimal.ZERO)
                    .walletStatus(WalletStatus.ACTIVE)
                    .build();
            
            walletRepository.save(Objects.requireNonNull(wallet));
            savedAdmin.setWallet(wallet);
            userRepository.save(savedAdmin);
            
            log.info("Bootstrap admin user seeded successfully for {}", adminEmail);
        } else {
            log.info("Admin user already exists. Skipping seeding.");
        }
    }
}
