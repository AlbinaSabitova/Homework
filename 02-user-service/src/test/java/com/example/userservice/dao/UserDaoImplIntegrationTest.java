package com.example.userservice.dao;

import com.example.userservice.dao.impl.UserDaoImpl;
import com.example.userservice.entity.User;
import com.example.userservice.exception.DaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserDaoImplIntegrationTest extends AbstractDaoIntegrationTest {

    private UserDaoImpl userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(sessionFactory);
    }

    // ---------------- save ----------------

    @Test
    @DisplayName("save: присваивает id и created_at")
    void save_persistsUser() {
        User saved = userDao.save(new User("Alice", "alice@mail.ru", 30));

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("save: дубликат email -> DaoException")
    void save_duplicateEmail_throws() {
        userDao.save(new User("Alice", "a@mail.ru", 30));

        assertThatThrownBy(() -> userDao.save(new User("Other", "a@mail.ru", 25)))
                .isInstanceOf(DaoException.class);
    }

    // ---------------- findById ----------------

    @Test
    @DisplayName("findById: возвращает сохранённого пользователя")
    void findById_returnsSaved() {
        User saved = userDao.save(new User("Alice", "a@mail.ru", 30));

        Optional<User> found = userDao.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("a@mail.ru");
    }

    @Test
    @DisplayName("findById: несуществующий id -> пустой Optional")
    void findById_missing_returnsEmpty() {
        assertThat(userDao.findById(9999L)).isEmpty();
    }

    // ---------------- findAll ----------------

    @Test
    @DisplayName("findAll: возвращает всех пользователей")
    void findAll_returnsAll() {
        userDao.save(new User("Alice", "a@mail.ru", 30));
        userDao.save(new User("Bob", "b@mail.ru", 25));

        List<User> all = userDao.findAll();

        assertThat(all).hasSize(2)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("a@mail.ru", "b@mail.ru");
    }

    @Test
    @DisplayName("findAll: пустая таблица -> пустой список")
    void findAll_empty_returnsEmptyList() {
        assertThat(userDao.findAll()).isEmpty();
    }

    // ---------------- update ----------------

    @Test
    @DisplayName("update: сохраняет изменения в БД")
    void update_persistsChanges() {
        User saved = userDao.save(new User("Alice", "a@mail.ru", 30));
        saved.setName("Alice Updated");
        saved.setAge(31);

        userDao.update(saved);

        User reloaded = userDao.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Alice Updated");
        assertThat(reloaded.getAge()).isEqualTo(31);
    }

    // ---------------- deleteById ----------------

    @Test
    @DisplayName("deleteById: удаляет пользователя")
    void deleteById_removes() {
        User saved = userDao.save(new User("Alice", "a@mail.ru", 30));

        boolean deleted = userDao.deleteById(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(userDao.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteById: несуществующий -> false")
    void deleteById_missing_returnsFalse() {
        assertThat(userDao.deleteById(9999L)).isFalse();
    }
}
