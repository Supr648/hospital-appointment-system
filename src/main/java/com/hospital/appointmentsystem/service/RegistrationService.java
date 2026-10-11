package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.AppUser;
import com.hospital.appointmentsystem.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class RegistrationService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String username, String password, String organizationName) {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new AccountAlreadyExistsException();
        }
        userRepository.save(new AppUser(normalizedUsername, passwordEncoder.encode(password),
                organizationName.trim(), false));
    }

    public static class AccountAlreadyExistsException extends RuntimeException {
    }
}
