package com.exchange_simulator.controller;

import com.exchange_simulator.mapper.UserMapper;
import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserNotFoundException;
import com.exchange_simulator.security.CustomUserDetails;
import com.exchange_simulator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal CustomUserDetails user){
        var found = userService.getUserById(user.getId());
        return found
                .map(u -> ResponseEntity.ok().body(userMapper.toDto(u)))
                .orElseThrow(() -> new UserNotFoundException(user.getId()));
    }
}
