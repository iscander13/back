package com.example.backend.config; // Или com.example.backend.util, или любой другой подходящий пакет

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.backend.entiity.User;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, существует ли администратор с email 'admin@example.com'
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@example.com") // Email администратора
                    .passwordHash(passwordEncoder.encode("admin123")) // Пароль администратора (будет хеширован)
                    .role("ADMIN") // Роль администратора
                    .build();
            userRepository.save(admin);
            System.out.println("Создан пользователь-администратор: admin@example.com");
        } else {
            System.out.println("Пользователь-администратор уже существует.");
        }
    }
}