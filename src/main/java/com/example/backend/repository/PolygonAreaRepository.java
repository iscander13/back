package com.example.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional; // Добавляем импорт

import com.example.backend.entiity.PolygonArea;

public interface PolygonAreaRepository extends JpaRepository<PolygonArea, UUID> {
    List<PolygonArea> findByUser_Id(Long userId);

    // НОВЫЙ МЕТОД: для удаления всех полигонов пользователя
    @Transactional // Обязательно для операций удаления
    void deleteByUser_Id(Long userId);
}