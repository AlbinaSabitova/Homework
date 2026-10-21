package com.example.userservice.service.impl;

import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.entity.User;
import com.example.userservice.event.UserEvent;
import com.example.userservice.event.UserEventProducer;
import com.example.userservice.event.UserOperation;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventProducer eventProducer;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, UserEventProducer eventProducer) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        User user = new User(request.name(), request.email(), request.age());
        User saved = userRepository.save(user);
        eventProducer.send(new UserEvent(UserOperation.CREATED, saved.getEmail()));
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) user.setEmail(request.email());
        if (request.age() != null) user.setAge(request.age());

        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String email = user.getEmail();
        userRepository.deleteById(id);

        eventProducer.send(new UserEvent(UserOperation.DELETED, email));
    }
}