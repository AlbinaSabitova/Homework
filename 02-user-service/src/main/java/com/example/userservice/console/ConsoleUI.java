package com.example.userservice.console;

import com.example.userservice.entity.User;
import com.example.userservice.exception.ServiceException;
import com.example.userservice.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {

    private final UserService userService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(UserService userService) {
        this.userService = userService;
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
            String name = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Возраст: ");
            Integer age = parseAge(scanner.nextLine());

            User saved = userService.create(name, email, age);
            System.out.println("Создан: " + saved);
        } catch (ServiceException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
        }
    }

    private void findUserById() {
        try {
            System.out.print("ID: ");
            Long id = parseId(scanner.nextLine());
            Optional<User> user = userService.findById(id);
            user.ifPresentOrElse(
                    u -> System.out.println("Найден: " + u),
                    () -> System.out.println("Пользователь с id=" + id + " не найден")
            );
        } catch (ServiceException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
        }
    }

    private void listAllUsers() {
        try {
            List<User> users = userService.findAll();
            if (users.isEmpty()) {
                System.out.println("Список пуст.");
            } else {
                users.forEach(System.out::println);
            }
        } catch (ServiceException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void updateUser() {
        try {
            System.out.print("ID пользователя для обновления: ");
            Long id = parseId(scanner.nextLine());

            Optional<User> found = userService.findById(id);
            if (found.isEmpty()) {
                System.out.println("Пользователь с id=" + id + " не найден");
                return;
            }
            User user = found.get();

            System.out.print("Новое имя (" + user.getName() + ", Enter — оставить): ");
            String nameInput = scanner.nextLine();
            String name = nameInput.isBlank() ? null : nameInput;

            System.out.print("Новый email (" + user.getEmail() + ", Enter — оставить): ");
            String emailInput = scanner.nextLine();
            String email = emailInput.isBlank() ? null : emailInput;

            System.out.print("Новый возраст (" + user.getAge() + ", Enter — оставить): ");
            String ageInput = scanner.nextLine();
            Integer age = ageInput.isBlank() ? null : parseAge(ageInput);

            User updated = userService.update(id, name, email, age);
            System.out.println("Обновлён: " + updated);
        } catch (ServiceException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
        }
    }

    private void deleteUser() {
        try {
            System.out.print("ID пользователя для удаления: ");
            Long id = parseId(scanner.nextLine());
            boolean deleted = userService.deleteById(id);
            System.out.println(deleted ? "Пользователь удалён" : "Пользователь не найден");
        } catch (ServiceException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
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
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Возраст должен быть числом");
        }
    }
}