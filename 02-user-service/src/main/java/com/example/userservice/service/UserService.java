package com.example.userservice.service;

import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    UserResponse findById(Long id);
    List<UserResponse> findAll();
    UserResponse update(Long id, UserUpdateRequest request);
    void deleteById(Long id);
}