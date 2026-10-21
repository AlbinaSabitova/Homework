package com.example.userservice.event;

public record UserEvent(
        UserOperation operation,
        String email
) {}