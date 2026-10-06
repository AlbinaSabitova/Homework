package com.example.userservice.service;

import com.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User create(String name, String email, Integer age);
    Optional<User> findById(Long id);
    List<User> findAll();
    User update(Long id, String name, String email, Integer age);
    boolean deleteById(Long id);
}
