package com.example.userservice.service.impl;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import com.example.userservice.exception.DaoException;
import com.example.userservice.exception.ServiceException;
import com.example.userservice.service.UserService;
import com.example.userservice.util.UserValidator;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User create(String name, String email, Integer age) {
        String validName = UserValidator.validateName(name);
        String validEmail = UserValidator.validateEmail(email);
        Integer validAge = UserValidator.validateAge(age);

        try {
            User user = new User(validName, validEmail, validAge);
            return userDao.save(user);
        } catch (DaoException e) {
            throw new ServiceException("Не удалось создать пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("id должен быть положительным числом");
        }
        try {
            return userDao.findById(id);
        } catch (DaoException e) {
            throw new ServiceException("Не удалось найти пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            return userDao.findAll();
        } catch (DaoException e) {
            throw new ServiceException("Не удалось получить список пользователей: " + e.getMessage(), e);
        }
    }

    @Override
    public User update(Long id, String name, String email, Integer age) {
        if (id == null || id <= 0) {
            throw new ServiceException("id должен быть положительным числом");
        }
        try {
            User existing = userDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Пользователь с id=" + id + " не найден"));

            if (name != null) existing.setName(UserValidator.validateName(name));
            if (email != null) existing.setEmail(UserValidator.validateEmail(email));
            if (age != null) existing.setAge(UserValidator.validateAge(age));

            return userDao.update(existing);
        } catch (DaoException e) {
            throw new ServiceException("Не удалось обновить пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("id должен быть положительным числом");
        }
        try {
            return userDao.deleteById(id);
        } catch (DaoException e) {
            throw new ServiceException("Не удалось удалить пользователя: " + e.getMessage(), e);
        }
    }
}