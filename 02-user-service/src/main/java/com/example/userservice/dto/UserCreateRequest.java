package com.example.userservice.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(

        @NotBlank(message = "Имя не может быть пустым")
        @Size(max = 100, message = "Имя слишком длинное")
        String name,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный email")
        @Size(max = 150, message = "Email слишком длинный")
        String email,

        @NotNull(message = "Возраст обязателен")
        @Min(value = 0, message = "Возраст должен быть >= 0")
        @Max(value = 150, message = "Возраст должен быть <= 150")
        Integer age
) {}