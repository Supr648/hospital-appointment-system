package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.AppUser;
import com.hospital.appointmentsystem.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void storesNormalizedAccountWithHashedPasswordAndPrivateWorkspaceName() {
        when(userRepository.existsByUsernameIgnoreCase("staff@example.com")).thenReturn(false);
        RegistrationService service = new RegistrationService(userRepository, passwordEncoder);

        service.register(" STAFF@EXAMPLE.COM ", "a-long-password-for-testing", " Northside Clinic ");

        ArgumentCaptor<AppUser> account = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(account.capture());
        assertTrue(account.getValue().getUsername().equals("staff@example.com"));
        assertTrue(passwordEncoder.matches("a-long-password-for-testing", account.getValue().getPassword()));
        assertFalse(account.getValue().getPassword().equals("a-long-password-for-testing"));
        assertTrue(account.getValue().getOrganizationName().equals("Northside Clinic"));
        assertFalse(account.getValue().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void rejectsDuplicateEmailWithoutSaving() {
        when(userRepository.existsByUsernameIgnoreCase("staff@example.com")).thenReturn(true);
        RegistrationService service = new RegistrationService(userRepository, passwordEncoder);

        org.junit.jupiter.api.Assertions.assertThrows(
                RegistrationService.AccountAlreadyExistsException.class,
                () -> service.register("staff@example.com", "a-long-password-for-testing", "Northside Clinic"));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }
}
