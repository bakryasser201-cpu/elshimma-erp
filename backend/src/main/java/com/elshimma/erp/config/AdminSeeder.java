package com.elshimma.erp.config;

import com.elshimma.erp.user.entity.Role;
import com.elshimma.erp.user.entity.User;
import com.elshimma.erp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@elshimma.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.initial-password}") String initialAdminPassword
    ) {
        if (initialAdminPassword == null || initialAdminPassword.isBlank()) {
            throw new IllegalStateException("Missing required configuration: ADMIN_INITIAL_PASSWORD must be set for initial admin creation.");
        }

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }

        User admin = User.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(initialAdminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
    }
}
