package com.example.userservice.console;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import com.example.userservice.exception.DaoException;
import com.example.userservice.util.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {

    private static final Logger log = LoggerFactory.getLogger(ConsoleUI.class);

    private final UserDao userDao;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(UserDao userDao) {
        this.userDao = userDao;
    }

    public void start() {
        printMenu();
        boolean running = true;
        while (running) {
            System.out.print("\nВыберите действие: ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> createUser();
                case "2" -> findUserById();
                case "3" -> listAllUsers();
                case "4" -> updateUser();
                case "5" -> deleteUser();
                case "0" -> running = false;
                default -> System.out.println("Неверный пункт. Попробуйте снова.");
            }
        }
        System.out.println("До свидания!");
    }

    private void printMenu() {
        System.out.println("""
                ===== User Service =====
                1. Создать пользователя
                2. Найти пользователя по id
                3. Показать всех пользователей
                4. Обновить пользователя
                5. Удалить пользователя
                0. Выход
                =========================
                """);
    }

    private void createUser() {
        try {
            System.out.print("Имя: ");
            String name = UserValidator.validateName(scanner.nextLine());

            System.out.print("Email: ");
            String email = UserValidator.validateEmail(scanner.nextLine());

            System.out.print("Возраст: ");
            Integer age = UserValidator.validateAge(parseAge(scanner.nextLine()));

            User user = new User(name, email, age);
            User saved = userDao.save(user);
            System.out.println("Создан: " + saved);
        } catch (DaoException e) {
            System.out.println("Ошибка БД: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
        }
    }

    private void findUserById() {
        try {
            System.out.print("ID: ");
            Long id = parseId(scanner.nextLine());
            Optional<User> user = userDao.findById(id);
            user.ifPresentOrElse(
                    u -> System.out.println("Найден: " + u),
                    () -> System.out.println("Пользователь с id=" + id + " не найден")
            );
        } catch (DaoException | IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void listAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("Список пуст.");
            } else {
                users.forEach(System.out::println);
            }
        } catch (DaoException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void updateUser() {
        try {
            System.out.print("ID пользователя для обновления: ");
            Long id = parseId(scanner.nextLine());
            Optional<User> found = userDao.findById(id);
            if (found.isEmpty()) {
                System.out.println("Пользователь с id=" + id + " не найден");
                return;
            }
            User user = found.get();

            System.out.print("Новое имя (" + user.getName() + ", Enter — оставить): ");
            String nameInput = scanner.nextLine();
            if (!nameInput.isBlank()) {
                user.setName(UserValidator.validateName(nameInput));
            }

            System.out.print("Новый email (" + user.getEmail() + ", Enter — оставить): ");
            String emailInput = scanner.nextLine();
            if (!emailInput.isBlank()) {
                user.setEmail(UserValidator.validateEmail(emailInput));
            }

            System.out.print("Новый возраст (" + user.getAge() + ", Enter — оставить): ");
            String ageInput = scanner.nextLine();
            if (!ageInput.isBlank()) {
                user.setAge(UserValidator.validateAge(parseAge(ageInput)));
            }

            User updated = userDao.update(user);
            System.out.println("Обновлён: " + updated);
        } catch (DaoException e) {
            System.out.println("Ошибка БД: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
        }
    }

    private void deleteUser() {
        try {
            System.out.print("ID пользователя для удаления: ");
            Long id = parseId(scanner.nextLine());
            boolean deleted = userDao.deleteById(id);
            System.out.println(deleted ? "Пользователь удалён" : "Пользователь не найден");
        } catch (DaoException | IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private Long parseId(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id должен быть числом");
        }
    }

    private Integer parseAge(String value) {
        if (value == null || value.isBlank()) {
            // возвращаем null — валидатор сам пожалуется «Возраст обязателен»
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Возраст должен быть числом");
        }
    }
}