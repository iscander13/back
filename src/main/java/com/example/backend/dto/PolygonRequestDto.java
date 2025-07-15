package com.example.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Этот DTO используется для входящих запросов (POST и PUT)
// Он должен содержать все поля, которые фронтенд отправляет на бэкенд
@Getter
@Setter
@NoArgsConstructor
public class PolygonRequestDto {
    private String name;
    private String geoJson;
    private String crop;
    private String comment;
    private String color; // НОВОЕ ПОЛЕ: для приема цвета от фронтенда
}
