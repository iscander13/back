package com.example.backend.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.entiity.PolygonArea;
import com.example.backend.entiity.User;
import com.example.backend.repository.PolygonAreaRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolygonService {

    private final PolygonAreaRepository polygonAreaRepository;
    private final UserRepository userRepository;

    public List<PolygonArea> getPolygonsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("PolygonService: User not authenticated.");
            throw new IllegalStateException("Пользователь не аутентифицирован.");
        }

        Object principal = authentication.getPrincipal();
        final String userEmail; // Объявляем userEmail как final или фактически final

        // Используем pattern matching для instanceof (Java 16+)
        if (principal instanceof User castedUser) {
            userEmail = castedUser.getEmail();
            log.info("PolygonService: Principal is User object, email: {}", userEmail);
        } else if (principal instanceof String emailString) {
            userEmail = emailString;
            log.warn("PolygonService: Principal is a String (email: {}), but expected User object. Check UserDetailsServiceImpl if this is unexpected.", userEmail);
        } else {
            // Проверяем principal на null, чтобы избежать Dereferencing possible null pointer hint
            log.error("PolygonService: Unexpected principal type: {}", principal != null ? principal.getClass().getName() : "null");
            throw new IllegalStateException("Не удалось получить email пользователя из контекста безопасности: Неизвестный тип Principal.");
        }

        if (userEmail == null || userEmail.isEmpty()) { // Добавлена проверка на isEmpty()
            log.error("PolygonService: User email not found or is empty in security context after extraction.");
            throw new IllegalStateException("Email пользователя не найден в контексте безопасности.");
        }

        User currentUser = userRepository.findByEmail(userEmail)
                                        .orElseThrow(() -> {
                                            log.error("PolygonService: Authenticated user {} not found in database.", userEmail);
                                            return new IllegalStateException("Authenticated user not found in database.");
                                        });
        
        log.info("PolygonService: Fetching polygons for user ID: {}", currentUser.getId());
        return polygonAreaRepository.findByUser_Id(currentUser.getId());
    }
}