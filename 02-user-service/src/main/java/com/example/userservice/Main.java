package com.example.userservice;

import com.example.userservice.console.ConsoleUI;
import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.impl.UserDaoImpl;
import com.example.userservice.service.UserService;
import com.example.userservice.service.impl.UserServiceImpl;
import com.example.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Запуск user-service");
        try {
            UserDao userDao = new UserDaoImpl(HibernateUtil.getSessionFactory());
            UserService userService = new UserServiceImpl(userDao);
            new ConsoleUI(userService).start();
        } catch (Exception e) {
            log.error("Критическая ошибка приложения", e);
            System.err.println("Критическая ошибка: " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
        }
    }
}