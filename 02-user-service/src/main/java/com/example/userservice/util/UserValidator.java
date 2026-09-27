package com.example.userservice.util;

import java.util.regex.Pattern;

public final class UserValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");

    private static final int NAME_MAX = 20;
    private static final int EMAIL_MAX = 50;
    private static final int AGE_MIN = 0;
    private static final int AGE_MAX = 150;

    private UserValidator() {
    }

    public static String validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Имя не может состоять из пробелов");
        }
        if (trimmed.length() > NAME_MAX) {
            throw new IllegalArgumentException("Имя слишком длинное (макс. " + NAME_MAX + " символов)");
        }
        return trimmed;
    }

    public static String validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Email не может состоять из пробелов");
        }
        if (trimmed.length() > EMAIL_MAX) {
            throw new IllegalArgumentException("Email слишком длинный (макс. " + EMAIL_MAX + " символов)");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Некорректный email: " + trimmed);
        }
        return trimmed;
    }

    public static Integer validateAge(Integer age) {
        if (age == null) {
            throw new IllegalArgumentException("Возраст обязателен");
        }
        if (age < AGE_MIN || age > AGE_MAX) {
            throw new IllegalArgumentException("Возраст должен быть в диапазоне " + AGE_MIN + ".." + AGE_MAX);
        }
        return age;
    }
}