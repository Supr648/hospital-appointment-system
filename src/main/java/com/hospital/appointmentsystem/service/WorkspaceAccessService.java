package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceAccessService {

    private final AppUserRepository userRepository;

    public WorkspaceAccessService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long workspaceId(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(user -> user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Account workspace not found."));
    }
}
