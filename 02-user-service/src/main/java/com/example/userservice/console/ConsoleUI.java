package com.example.userservice.console;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import com.example.userservice.exception.DaoException;
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
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Возраст: ");
            Integer age = parseAge(scanner.nextLine());

            User user = new User(name, email, age);
            User saved = userDao.save(user);
            System.out.println("Создан: " + saved);
        } catch (DaoException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Некорректный ввод: " + e.getMessage());
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

            System.out.print("Новое имя (" + user.getName() + "): ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) user.setName(name);

            System.out.print("Новый email (" + user.getEmail() + "): ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) user.setEmail(email);

            System.out.print("Новый возраст (" + user.getAge() + "): ");
            String ageLine = scanner.nextLine().trim();
            if (!ageLine.isEmpty()) user.setAge(parseAge(ageLine));

            User updated = userDao.update(user);
            System.out.println("Обновлён: " + updated);
        } catch (DaoException | IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
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
        if (value == null || value.isBlank()) return null;
        try {
            int age = Integer.parseInt(value.trim());
            if (age < 0 || age > 150) {
                throw new IllegalArgumentException("возраст должен быть в диапазоне 0..150");
            }
            return age;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("возраст должен быть числом");
        }
    }
}