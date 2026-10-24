package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на создание пользователя")
public record UserCreateRequest(

        @Schema(description = "Имя пользователя", example = "Alice", required = true)
        @NotBlank(message = "Имя не может быть пустым")
        @Size(max = 100, message = "Имя слишком длинное")
        String name,

        @Schema(description = "Email пользователя", example = "alice@mail.ru", required = true)
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный email")
        @Size(max = 150, message = "Email слишком длинный")
        String email,

        @Schema(description = "Возраст", example = "30", minimum = "0", maximum = "150")
        @NotNull(message = "Возраст обязателен")
        @Min(value = 0, message = "Возраст должен быть >= 0")
        @Max(value = 150, message = "Возраст должен быть <= 150")
        Integer age
) {}