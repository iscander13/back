package com.example.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.entiity.User;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.PolygonAreaRepository;
import com.example.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolygonAreaRepository polygonAreaRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public String updateUserEmail(Long userId, String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден.");
        }

        User user = userOptional.get();
        if (userRepository.findByEmail(newEmail).isPresent() && !user.getEmail().equals(newEmail)) {
            throw new RuntimeException("Пользователь с email " + newEmail + " уже существует.");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
        return "Email пользователя " + userId + " успешно обновлен на " + newEmail;
    }

    @Transactional
    public String resetUserPassword(Long userId, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден.");
        }

        User user = userOptional.get();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Пароль пользователя " + userId + " успешно обновлен.";
    }

    @Transactional
    public String deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден.");
        }

        chatMessageRepository.deleteByUser_Id(userId);
        polygonAreaRepository.deleteByUser_Id(userId);
        userRepository.deleteById(userId);
        return "Пользователь с ID " + userId + " и все его данные успешно удалены.";
    }

    // НОВЫЙ МЕТОД: Изменение роли пользователя
    @Transactional
    public String updateUserRole(Long userId, String newRole) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден.");
        }

        User user = userOptional.get();
        // Приводим новую роль к верхнему регистру, чтобы избежать ошибок с "admin" vs "ADMIN"
        String normalizedNewRole = newRole.toUpperCase();

        // Проверяем, что новая роль является одной из допустимых (например, "USER", "ADMIN")
        if (!normalizedNewRole.equals("USER") && !normalizedNewRole.equals("ADMIN")) {
            throw new IllegalArgumentException("Недопустимая роль: " + newRole + ". Допустимые роли: USER, ADMIN.");
        }

        user.setRole(normalizedNewRole);
        userRepository.save(user);
        return "Роль пользователя " + userId + " успешно изменена на " + normalizedNewRole + ".";
    }
}