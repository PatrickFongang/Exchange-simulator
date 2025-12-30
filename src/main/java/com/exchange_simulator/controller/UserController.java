package com.exchange_simulator.controller;

import com.exchange_simulator.dto.user.UserCreateRequestDto;
import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(){
        return ResponseEntity.ok(userService
                .getUsers()
                .stream()
                .map(UserService::getDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") Long id){
        var user = userService.getUserById(id);
        return user
                .map(value -> ResponseEntity.ok().body(UserService.getDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserCreateRequestDto userData){
        var user = userService.createUser(userData);
        return ResponseEntity.ok(UserService.getDto(user));
    }
}
