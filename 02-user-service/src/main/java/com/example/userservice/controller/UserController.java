package com.example.userservice.controller;

import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список получен")
    @GetMapping
    public CollectionModel<EntityModel<UserResponse>> findAll() {
        List<EntityModel<UserResponse>> users = userService.findAll().stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(users,
                linkTo(methodOn(UserController.class).findAll()).withSelfRel());
    }

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public EntityModel<UserResponse> findById(
            @Parameter(description = "ID пользователя", example = "1") @PathVariable Long id) {
        return toModel(userService.findById(id));
    }

    @Operation(summary = "Создать пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.create(request);
        EntityModel<UserResponse> model = toModel(created);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.id()))
                .body(model);
    }

    @Operation(summary = "Обновить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}")
    public EntityModel<UserResponse> update(
            @Parameter(description = "ID пользователя", example = "1") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return toModel(userService.update(id, request));
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "ID пользователя", example = "1") @PathVariable Long id) {
        userService.deleteById(id);
    }

    private EntityModel<UserResponse> toModel(UserResponse user) {
        Long id = user.id();
        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).findById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).findAll()).withRel("users"),
                linkTo(methodOn(UserController.class).update(id, null)).withRel("update"),
                Link.of("/api/users/" + id, "delete")
        );
    }
}