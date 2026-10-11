package com.example.userservice.dto;

import jakarta.validation.constraints.*;

public record UserUpdateRequest(

        @Size(max = 100, message = "Имя слишком длинное")
        String name,               // null = не менять

        @Email(message = "Некорректный email")
        @Size(max = 150, message = "Email слишком длинный")
        String email,              // null = не менять

        @Min(value = 0, message = "Возраст должен быть >= 0")
        @Max(value = 150, message = "Возраст должен быть <= 150")
        Integer age                // null = не менять
) {}