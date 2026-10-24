package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Данные пользователя")
public record UserResponse(
        @Schema(description = "ID пользователя", example = "1")
        Long id,
        @Schema(description = "Имя", example = "Alice")
        String name,
        @Schema(description = "Email", example = "alice@mail.ru")
        String email,
        @Schema(description = "Возраст", example = "30")
        Integer age,
        @Schema(description = "Дата создания", example = "2026-10-22T12:00:00")
        LocalDateTime createdAt
) {}