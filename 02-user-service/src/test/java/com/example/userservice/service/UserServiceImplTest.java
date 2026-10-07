package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import com.example.userservice.exception.DaoException;
import com.example.userservice.exception.ServiceException;
import com.example.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice", "alice@mail.ru", 30);
        sampleUser.setId(1L);
    }

    // ---------------- create ----------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("валидные данные -> userDao.save вызван с корректным User")
        void valid_savesViaDao() {
            when(userDao.save(any(User.class))).thenReturn(sampleUser);

            User result = userService.create("Alice", "alice@mail.ru", 30);

            assertThat(result).isSameAs(sampleUser);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userDao).save(captor.capture());
            User passed = captor.getValue();
            assertThat(passed.getName()).isEqualTo("Alice");
            assertThat(passed.getEmail()).isEqualTo("alice@mail.ru");
            assertThat(passed.getAge()).isEqualTo(30);

            verifyNoMoreInteractions(userDao);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("пустое имя -> IllegalArgumentException, DAO не вызывается")
        void blankName_throws(String name) {
            assertThatThrownBy(() -> userService.create(name, "a@mail.ru", 30))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(userDao);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "a@", "@mail.ru", "a@mail"})
        @DisplayName("некорректный email -> IllegalArgumentException")
        void invalidEmail_throws(String email) {
            assertThatThrownBy(() -> userService.create("Alice", email, 30))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(userDao);
        }

        @Test
        @DisplayName("возраст null -> IllegalArgumentException")
        void nullAge_throws() {
            assertThatThrownBy(() -> userService.create("Alice", "a@mail.ru", null))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(userDao);
        }

        @Test
        @DisplayName("DAO бросает DaoException -> ServiceException")
        void daoFailure_wrappedIntoServiceException() {
            when(userDao.save(any(User.class)))
                    .thenThrow(new DaoException("DB is down"));

            assertThatThrownBy(() -> userService.create("Alice", "a@mail.ru", 30))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Не удалось создать пользователя")
                    .hasCauseInstanceOf(DaoException.class);
        }
    }

    // ---------------- findById ----------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("существующий id -> Optional с пользователем")
        void existing_returnsUser() {
            when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

            assertThat(userService.findById(1L)).contains(sampleUser);
            verify(userDao).findById(1L);
        }

        @Test
        @DisplayName("несуществующий id -> пустой Optional")
        void missing_returnsEmpty() {
            when(userDao.findById(99L)).thenReturn(Optional.empty());

            assertThat(userService.findById(99L)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("невалидный id -> ServiceException, DAO не вызывается")
        void invalidId_throws(long id) {
            assertThatThrownBy(() -> userService.findById(id))
                    .isInstanceOf(ServiceException.class);

            verifyNoInteractions(userDao);
        }

        @Test
        @DisplayName("id null -> ServiceException")
        void nullId_throws() {
            assertThatThrownBy(() -> userService.findById(null))
                    .isInstanceOf(ServiceException.class);

            verifyNoInteractions(userDao);
        }

        @Test
        @DisplayName("DAO бросает DaoException -> ServiceException")
        void daoFailure_wrapped() {
            when(userDao.findById(1L)).thenThrow(new DaoException("boom"));

            assertThatThrownBy(() -> userService.findById(1L))
                    .isInstanceOf(ServiceException.class)
                    .hasCauseInstanceOf(DaoException.class);
        }
    }

    // ---------------- findAll ----------------

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("возвращает список из DAO")
        void returnsList() {
            when(userDao.findAll()).thenReturn(List.of(sampleUser));

            assertThat(userService.findAll()).containsExactly(sampleUser);
        }

        @Test
        @DisplayName("пустая БД -> пустой список")
        void empty_returnsEmptyList() {
            when(userDao.findAll()).thenReturn(List.of());

            assertThat(userService.findAll()).isEmpty();
        }

        @Test
        @DisplayName("DAO бросает DaoException -> ServiceException")
        void daoFailure_wrapped() {
            when(userDao.findAll()).thenThrow(new DaoException("boom"));

            assertThatThrownBy(() -> userService.findAll())
                    .isInstanceOf(ServiceException.class)
                    .hasCauseInstanceOf(DaoException.class);
        }
    }

    // ---------------- update ----------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("валидные данные -> DAO.update с обновлённым User")
        void valid_updatesUser() {
            when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userDao.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.update(1L, "Bob", "bob@mail.ru", 25);

            assertThat(result.getName()).isEqualTo("Bob");
            assertThat(result.getEmail()).isEqualTo("bob@mail.ru");
            assertThat(result.getAge()).isEqualTo(25);

            verify(userDao).update(sampleUser);
        }

        @Test
        @DisplayName("null-поля не меняют прежние значения")
        void nullFields_keepOldValues() {
            when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userDao.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.update(1L, null, null, null);

            assertThat(result.getName()).isEqualTo("Alice");
            assertThat(result.getEmail()).isEqualTo("alice@mail.ru");
            assertThat(result.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("несуществующий id -> ServiceException, update не вызывается")
        void missingUser_throws() {
            when(userDao.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(42L, "X", "x@mail.ru", 20))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("не найден");

            verify(userDao, never()).update(any());
        }

        @Test
        @DisplayName("некорректный email -> IllegalArgumentException, update не вызывается")
        void invalidEmail_throws() {
            when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.update(1L, null, "bad", null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userDao, never()).update(any());
        }

        @Test
        @DisplayName("DAO бросает DaoException при findById -> ServiceException")
        void daoFindFailure_wrapped() {
            when(userDao.findById(1L)).thenThrow(new DaoException("boom"));

            assertThatThrownBy(() -> userService.update(1L, "X", "x@mail.ru", 20))
                    .isInstanceOf(ServiceException.class)
                    .hasCauseInstanceOf(DaoException.class);
        }
    }

    // ---------------- deleteById ----------------

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("существующий -> true")
        void existing_returnsTrue() {
            when(userDao.deleteById(1L)).thenReturn(true);

            assertThat(userService.deleteById(1L)).isTrue();
            verify(userDao).deleteById(1L);
        }

        @Test
        @DisplayName("несуществующий -> false")
        void missing_returnsFalse() {
            when(userDao.deleteById(99L)).thenReturn(false);

            assertThat(userService.deleteById(99L)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("невалидный id -> ServiceException")
        void invalidId_throws(long id) {
            assertThatThrownBy(() -> userService.deleteById(id))
                    .isInstanceOf(ServiceException.class);

            verifyNoInteractions(userDao);
        }

        @Test
        @DisplayName("DAO бросает DaoException -> ServiceException")
        void daoFailure_wrapped() {
            when(userDao.deleteById(1L)).thenThrow(new DaoException("boom"));

            assertThatThrownBy(() -> userService.deleteById(1L))
                    .isInstanceOf(ServiceException.class)
                    .hasCauseInstanceOf(DaoException.class);
        }
    }
}