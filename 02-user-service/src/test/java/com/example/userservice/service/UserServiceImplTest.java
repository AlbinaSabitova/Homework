package com.example.userservice.service;

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
import com.example.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserEventProducer eventProducer;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice", "alice@mail.ru", 30);
        sampleUser.setId(1L);
        sampleUser.setCreatedAt(LocalDateTime.now());

        sampleResponse = new UserResponse(
                1L, "Alice", "alice@mail.ru", 30, sampleUser.getCreatedAt()
        );
    }

    // ---------------- create ----------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("валидные данные -> сохраняет, возвращает DTO и отправляет CREATED-событие")
        void valid_savesReturnsDtoAndSendsEvent() {
            UserCreateRequest request = new UserCreateRequest("Alice", "alice@mail.ru", 30);

            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

            UserResponse result = userService.create(request);

            assertThat(result).isEqualTo(sampleResponse);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User passed = userCaptor.getValue();
            assertThat(passed.getName()).isEqualTo("Alice");
            assertThat(passed.getEmail()).isEqualTo("alice@mail.ru");
            assertThat(passed.getAge()).isEqualTo(30);

            verify(userMapper).toResponse(sampleUser);

            ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
            verify(eventProducer).send(eventCaptor.capture());
            UserEvent sentEvent = eventCaptor.getValue();
            assertThat(sentEvent.operation()).isEqualTo(UserOperation.CREATED);
            assertThat(sentEvent.email()).isEqualTo("alice@mail.ru");
        }

        @Test
        @DisplayName("save вызывается раньше, чем send события")
        void valid_saveBeforeSendEvent() {
            UserCreateRequest request = new UserCreateRequest("Alice", "alice@mail.ru", 30);

            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

            userService.create(request);

            InOrder inOrder = inOrder(userRepository, eventProducer);
            inOrder.verify(userRepository).save(any(User.class));
            inOrder.verify(eventProducer).send(any(UserEvent.class));
        }

        @Test
        @DisplayName("ошибка репозитория -> пробрасывается наверх, событие НЕ отправляется")
        void repositoryFails_propagatesAndDoesNotSendEvent() {
            UserCreateRequest request = new UserCreateRequest("Alice", "alice@mail.ru", 30);
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");

            verify(eventProducer, never()).send(any());
        }
    }

    // ---------------- findById ----------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("существующий id -> возвращает DTO, событий нет")
        void existing_returnsDto() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

            UserResponse result = userService.findById(1L);

            assertThat(result).isEqualTo(sampleResponse);
            verify(userRepository).findById(1L);
            verify(userMapper).toResponse(sampleUser);
            verifyNoInteractions(eventProducer);
        }

        @Test
        @DisplayName("несуществующий id -> UserNotFoundException")
        void missing_throws() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");

            verify(userMapper, never()).toResponse(any());
            verifyNoInteractions(eventProducer);
        }
    }

    // ---------------- findAll ----------------

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("возвращает список DTO")
        void returnsListOfDtos() {
            when(userRepository.findAll()).thenReturn(List.of(sampleUser));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

            List<UserResponse> result = userService.findAll();

            assertThat(result).containsExactly(sampleResponse);
            verifyNoInteractions(eventProducer);
        }

        @Test
        @DisplayName("пустая БД -> пустой список")
        void empty_returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.findAll()).isEmpty();
            verify(userMapper, never()).toResponse(any());
            verifyNoInteractions(eventProducer);
        }
    }

    // ---------------- update ----------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("валидные данные -> обновляет и возвращает DTO, событие НЕ отправляется")
        void valid_updatesAndReturnsDtoWithoutEvent() {
            UserUpdateRequest request = new UserUpdateRequest("Bob", "bob@mail.ru", 25);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(sampleUser)).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(
                    new UserResponse(1L, "Bob", "bob@mail.ru", 25, sampleUser.getCreatedAt())
            );

            UserResponse result = userService.update(1L, request);

            assertThat(result.name()).isEqualTo("Bob");
            assertThat(result.email()).isEqualTo("bob@mail.ru");
            assertThat(result.age()).isEqualTo(25);

            assertThat(sampleUser.getName()).isEqualTo("Bob");
            assertThat(sampleUser.getEmail()).isEqualTo("bob@mail.ru");
            assertThat(sampleUser.getAge()).isEqualTo(25);

            verify(userRepository).save(sampleUser);
            verifyNoInteractions(eventProducer);
        }

        @Test
        @DisplayName("null-поля -> оставляют прежние значения")
        void nullFields_keepOldValues() {
            UserUpdateRequest request = new UserUpdateRequest(null, null, null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(sampleUser)).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

            userService.update(1L, request);

            assertThat(sampleUser.getName()).isEqualTo("Alice");
            assertThat(sampleUser.getEmail()).isEqualTo("alice@mail.ru");
            assertThat(sampleUser.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("частичное обновление — меняет только переданное поле")
        void partialUpdate_changesOnlyProvidedField() {
            UserUpdateRequest request = new UserUpdateRequest(null, "new@mail.ru", null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(sampleUser)).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

            userService.update(1L, request);

            assertThat(sampleUser.getName()).isEqualTo("Alice");
            assertThat(sampleUser.getEmail()).isEqualTo("new@mail.ru");
            assertThat(sampleUser.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("несуществующий id -> UserNotFoundException, save не вызывается")
        void missing_throws() {
            UserUpdateRequest request = new UserUpdateRequest("Bob", null, null);

            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(99L, request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
            verifyNoInteractions(eventProducer);
        }
    }

    // ---------------- deleteById ----------------

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("существующий -> удаляет и отправляет DELETED-событие с email")
        void existing_deletesAndSendsEvent() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            userService.deleteById(1L);

            verify(userRepository).deleteById(1L);

            ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
            verify(eventProducer).send(eventCaptor.capture());
            UserEvent sentEvent = eventCaptor.getValue();
            assertThat(sentEvent.operation()).isEqualTo(UserOperation.DELETED);
            assertThat(sentEvent.email()).isEqualTo("alice@mail.ru");
        }

        @Test
        @DisplayName("несуществующий -> UserNotFoundException, deleteById и событие НЕ вызываются")
        void missing_throwsAndDoesNotSendEvent() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteById(99L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).deleteById(any());
            verify(eventProducer, never()).send(any());
        }
    }
}