package com.example.notificationservice.dto;

public record UserEvent(
        Operation operation,
        String email
) {
    public enum Operation {
        CREATED, DELETED
    }
}