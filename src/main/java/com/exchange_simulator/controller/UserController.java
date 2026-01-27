package com.exchange_simulator.controller;

import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserNotFoundException;
import com.exchange_simulator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(){
        return ResponseEntity.ok(userService
                .getUsers()
                .stream()
                .map(UserService::getDto)
                .toList());
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") Long id){
        var user = userService.getUserById(id);
        return user
                .map(value -> ResponseEntity.ok().body(UserService.getDto(value)))
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
