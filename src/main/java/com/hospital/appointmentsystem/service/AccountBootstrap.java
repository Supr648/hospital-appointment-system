package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.AppUser;
import com.hospital.appointmentsystem.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountBootstrap implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AccountBootstrap(AppUserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.username:}") String adminUsername,
                            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername.trim();
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminUsername.isBlank() || adminPassword.length() < 16) {
            throw new IllegalStateException(
                    "Set APP_ADMIN_USERNAME and an APP_ADMIN_PASSWORD of at least 16 characters.");
        }
        if (!userRepository.existsByUsernameIgnoreCase(adminUsername)) {
            userRepository.save(new AppUser(adminUsername.toLowerCase(),
                    passwordEncoder.encode(adminPassword), "Hospital workspace", true));
        }
    }
}
