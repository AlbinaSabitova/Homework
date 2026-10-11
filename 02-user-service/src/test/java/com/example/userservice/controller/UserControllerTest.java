package com.example.userservice.controller;

import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;

    private final UserResponse sample = new UserResponse(
            1L, "Alice", "alice@mail.ru", 30, LocalDateTime.now()
    );

    // -------- GET /api/users --------

    @Test
    @DisplayName("GET /api/users -> 200 + список")
    void findAll_returnsList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@mail.ru"));
    }

    // -------- GET /api/users/{id} --------

    @Test
    @DisplayName("GET /api/users/{id} -> 200 + пользователь")
    void findById_returnsUser() throws Exception {
        when(userService.findById(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@mail.ru"));
    }

    @Test
    @DisplayName("GET /api/users/{id} несуществующий -> 404")
    void findById_missing_returns404() throws Exception {
        when(userService.findById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------- POST /api/users --------

    @Test
    @DisplayName("POST /api/users -> 201 + Location")
    void create_returns201() throws Exception {
        when(userService.create(any())).thenReturn(sample);

        String body = """
                {"name":"Alice","email":"alice@mail.ru","age":30}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/users с невалидным email -> 400 + детали")
    void create_invalidEmail_returns400() throws Exception {
        String body = """
                {"name":"Alice","email":"bad-email","age":30}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.email").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/users с пустым name -> 400")
    void create_blankName_returns400() throws Exception {
        String body = """
                {"name":"","email":"alice@mail.ru","age":30}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -------- PUT /api/users/{id} --------

    @Test
    @DisplayName("PUT /api/users/{id} -> 200")
    void update_returns200() throws Exception {
        when(userService.update(eq(1L), any())).thenReturn(sample);

        String body = """
                {"name":"Bob","email":"bob@mail.ru","age":25}
                """;

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // -------- DELETE /api/users/{id} --------

    @Test
    @DisplayName("DELETE /api/users/{id} -> 204")
    void delete_returns204() throws Exception {
        doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} несуществующий -> 404")
    void delete_missing_returns404() throws Exception {
        doThrow(new UserNotFoundException(99L)).when(userService).deleteById(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}