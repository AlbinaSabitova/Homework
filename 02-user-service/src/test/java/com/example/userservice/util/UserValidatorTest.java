package com.example.userservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class UserValidatorTest {

    @Nested
    @DisplayName("validateName")
    class ValidateName {

        @Test
        @DisplayName("корректное имя возвращается с trim")
        void validName_trimmed() {
            assertThat(UserValidator.validateName("  Alice  ")).isEqualTo("Alice");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("пустые/пробельные значения -> IllegalArgumentException")
        void blankOrNull_throws(String input) {
            assertThatThrownBy(() -> UserValidator.validateName(input))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("имя длиннее NAME_MAX -> IllegalArgumentException")
        void tooLong_throws() {
            String tooLong = "a".repeat(21); // NAME_MAX = 20
            assertThatThrownBy(() -> UserValidator.validateName(tooLong))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("длинн");
        }

        @Test
        @DisplayName("имя ровно NAME_MAX символов проходит")
        void exactMaxLength_passes() {
            String exact = "a".repeat(20);
            assertThat(UserValidator.validateName(exact)).hasSize(20);
        }
    }

    @Nested
    @DisplayName("validateEmail")
    class ValidateEmail {

        @ParameterizedTest
        @ValueSource(strings = {
                "a@mail.ru",
                "user@example.com",
                "test.user+tag@sub.domain.co",
                "u@d.io"
        })
        @DisplayName("корректные email проходят")
        void validEmail_passes(String email) {
            assertThat(UserValidator.validateEmail(email)).isEqualTo(email);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   ",
                "abc",
                "a@",
                "@mail.ru",
                "a@mail",
                "a b@mail.ru",
                "a@@mail.ru",
                "a@mail..ru"
        })
        @DisplayName("некорректные email -> IllegalArgumentException")
        void invalidEmail_throws(String email) {
            assertThatThrownBy(() -> UserValidator.validateEmail(email))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("email с пробелами по краям тримится")
        void emailWithSpaces_trimmed() {
            assertThat(UserValidator.validateEmail("  a@mail.ru  "))
                    .isEqualTo("a@mail.ru");
        }

        @Test
        @DisplayName("email длиннее EMAIL_MAX -> IllegalArgumentException")
        void tooLong_throws() {
            String longEmail = "a".repeat(45) + "@mail.ru"; // > 50
            assertThatThrownBy(() -> UserValidator.validateEmail(longEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("длинн");
        }
    }

    @Nested
    @DisplayName("validateAge")
    class ValidateAge {

        @Test
        @DisplayName("null -> IllegalArgumentException")
        void nullAge_throws() {
            assertThatThrownBy(() -> UserValidator.validateAge(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("обязател");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 30, 149, 150})
        @DisplayName("граничные и обычные значения проходят")
        void validAges_pass(int age) {
            assertThat(UserValidator.validateAge(age)).isEqualTo(age);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -100, 151, 200})
        @DisplayName("значения вне диапазона -> IllegalArgumentException")
        void outOfRange_throws(int age) {
            assertThatThrownBy(() -> UserValidator.validateAge(age))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("диапазон");
        }
    }
}