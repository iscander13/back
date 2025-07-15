package com.example.backend.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.PolygonAreaResponseDto;
import com.example.backend.dto.PolygonRequestDto;
import com.example.backend.entiity.PolygonArea;
import com.example.backend.entiity.User;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.PolygonAreaRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PolygonService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/polygons")
@Slf4j
public class PolygonAreaController {

    private final PolygonAreaRepository polygonAreaRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PolygonService polygonService;

    public PolygonAreaController(PolygonAreaRepository polygonAreaRepository,
                                 ChatMessageRepository chatMessageRepository,
                                 UserRepository userRepository,
                                 PolygonService polygonService) {
        this.polygonAreaRepository = polygonAreaRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.polygonService = polygonService;
    }

    @PutMapping("/{id}") // <-- ЭТО КЛЮЧЕВАЯ АННОТАЦИЯ И ПУТЬ
@Transactional
public ResponseEntity<PolygonAreaResponseDto> updatePolygon(@PathVariable UUID id,
                                                            @RequestBody PolygonRequestDto polygonRequestDto,
                                                            @AuthenticationPrincipal User principalUser) {
    log.info("PolygonAreaController: Received request to update polygon with ID: {} for user: {}", id, principalUser != null ? principalUser.getEmail() : "null");

    if (principalUser == null || principalUser.getUsername() == null) {
        log.warn("Unauthorized attempt to update polygon: No principal user or user email found.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PolygonAreaResponseDto("Unauthorized"));
    }

    // Повторное получение User из репозитория для гарантии полной инициализации
    User currentUser = userRepository.findByEmail(principalUser.getUsername())
                                    .orElseThrow(() -> {
                                        log.error("Authenticated user {} not found in database during polygon update.", principalUser.getUsername());
                                        return new IllegalStateException("Authenticated user not found in database.");
                                    });

    try {
        Optional<PolygonArea> existingPolygonOptional = polygonAreaRepository.findById(id);
        if (existingPolygonOptional.isEmpty()) {
            log.warn("Polygon with ID {} not found for update.", id);
            return ResponseEntity.notFound().build();
        }

        PolygonArea existingPolygon = existingPolygonOptional.get();

        // Проверяем, что полигон принадлежит текущему пользователю
        if (!existingPolygon.getUser().getId().equals(currentUser.getId())) {
            log.warn("Attempt to update polygon with ID {} by user {} failed: User is not the owner.", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PolygonAreaResponseDto("You are not authorized to update this polygon."));
        }

        // Обновляем поля полигона из DTO
        existingPolygon.setName(polygonRequestDto.getName());
        existingPolygon.setComment(polygonRequestDto.getComment());
        existingPolygon.setColor(polygonRequestDto.getColor());
        existingPolygon.setCrop(polygonRequestDto.getCrop());
        existingPolygon.setGeoJson(polygonRequestDto.getGeoJson()); // Обновляем GeoJSON

        PolygonArea updatedPolygon = polygonAreaRepository.save(existingPolygon);
        log.info("Polygon with ID {} updated successfully for user {}.", id, currentUser.getEmail());
        return ResponseEntity.ok(new PolygonAreaResponseDto(updatedPolygon));

    } catch (Exception e) {
        log.error("Error updating polygon with ID {} for user {}: {}", id, currentUser.getEmail(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PolygonAreaResponseDto("Failed to update polygon: " + e.getMessage()));
    }
}

    @GetMapping("/user")
    public ResponseEntity<List<PolygonAreaResponseDto>> getPolygonsForCurrentUser(@AuthenticationPrincipal User principalUser) {
        log.info("PolygonAreaController: Received request to get all polygons for user: {}", principalUser != null ? principalUser.getEmail() : "null");

        if (principalUser == null || principalUser.getUsername() == null) {
            log.warn("No principal user or user email found in security context for getting polygons.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        User currentUser = userRepository.findByEmail(principalUser.getUsername())
                                        .orElseThrow(() -> {
                                            log.error("Authenticated user {} not found in database during polygon retrieval.", principalUser.getUsername());
                                            return new IllegalStateException("Authenticated user not found in database.");
                                        });
        
        List<PolygonArea> polygons = polygonAreaRepository.findByUser_Id(currentUser.getId());
        
        // --- ИЗМЕНЕНИЕ ЗДЕСЬ: Используем конструктор PolygonAreaResponseDto(PolygonArea) ---
        List<PolygonAreaResponseDto> response = polygons.stream()
                .map(PolygonAreaResponseDto::new) // Теперь этот конструктор существует
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @Transactional
    // --- ИЗМЕНЕНИЕ ЗДЕСЬ: Возвращаем ResponseEntity<String> для ошибок ---
    public ResponseEntity<?> createPolygon(@RequestBody PolygonRequestDto polygonRequestDto, @AuthenticationPrincipal User principalUser) {
        log.info("PolygonAreaController: Received request to create polygon for user: {}", principalUser != null ? principalUser.getEmail() : "null");

        if (principalUser == null || principalUser.getUsername() == null) {
            log.warn("No principal user or user email found in security context for polygon creation.");
            // --- ИЗМЕНЕНИЕ ЗДЕСЬ ---
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User currentUser = userRepository.findByEmail(principalUser.getUsername())
                                        .orElseThrow(() -> {
                                            log.error("Authenticated user {} not found in database during polygon creation.", principalUser.getUsername());
                                            return new IllegalStateException("Authenticated user not found in database.");
                                        });
        
        try {
            PolygonArea polygonArea = PolygonArea.builder()
                    .id(UUID.randomUUID())
                    .name(polygonRequestDto.getName())
                    .comment(polygonRequestDto.getComment())
                    .color(polygonRequestDto.getColor())
                    .crop(polygonRequestDto.getCrop())
                    .geoJson(polygonRequestDto.getGeoJson())
                    .user(currentUser)
                    .build();

            PolygonArea savedPolygon = polygonAreaRepository.save(polygonArea);
            // --- ИЗМЕНЕНИЕ ЗДЕСЬ: Используем конструктор PolygonAreaResponseDto(PolygonArea) ---
            return ResponseEntity.status(HttpStatus.CREATED).body(new PolygonAreaResponseDto(savedPolygon));
        } catch (Exception e) {
            log.error("Error creating polygon for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            // --- ИЗМЕНЕНИЕ ЗДЕСЬ ---
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create polygon: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity<?> deletePolygon(@PathVariable UUID id, @AuthenticationPrincipal User principalUser) {
        log.info("PolygonAreaController: Received request to delete polygon with ID: {} for user: {}", id, principalUser != null ? principalUser.getEmail() : "null");

        if (principalUser == null || principalUser.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User currentUser = userRepository.findByEmail(principalUser.getUsername())
                                        .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        try {
            Optional<PolygonArea> polygonOptional = polygonAreaRepository.findById(id);
            if (polygonOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            PolygonArea polygon = polygonOptional.get();

            if (!polygon.getUser().getId().equals(currentUser.getId())) {
                log.warn("Attempt to delete polygon with ID {} by user {} failed: User is not the owner.", id, currentUser.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this polygon.");
            }

            chatMessageRepository.deleteByPolygonArea_IdAndUser_Id(polygon.getId(), currentUser.getId());
            polygonAreaRepository.deleteById(id);
            log.info("Polygon with ID {} deleted successfully for user {}.", id, currentUser.getEmail());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting polygon with ID {} for user {}: {}", id, currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete polygon due to internal server error: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear-all")
    @Transactional
    public ResponseEntity<?> deleteAllPolygonsForUser(@AuthenticationPrincipal User principalUser) {
        log.info("PolygonAreaController: Received request to clear all polygons for user.");

        if (principalUser == null || principalUser.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User currentUser = userRepository.findByEmail(principalUser.getUsername())
                                        .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        try {
            List<PolygonArea> userPolygons = polygonAreaRepository.findByUser_Id(currentUser.getId());
            if (userPolygons.isEmpty()) {
                log.info("No polygons found to delete for user {}. Skipping clear all.", currentUser.getEmail());
                return ResponseEntity.noContent().build();
            }

            for (PolygonArea polygon : userPolygons) {
                chatMessageRepository.deleteByPolygonArea_IdAndUser_Id(polygon.getId(), currentUser.getId());
            }

            polygonAreaRepository.deleteAll(userPolygons);
            log.info("All polygons cleared successfully for user {}. Total {} polygons deleted.", currentUser.getEmail(), userPolygons.size());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error clearing all polygons for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to clear all polygons due to internal server error: " + e.getMessage());
        }
    }
}