package com.exchange_simulator.controller;

import com.exchange_simulator.mapper.UserMapper;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserNotFoundException;
import com.exchange_simulator.service.OrderService;
import com.exchange_simulator.service.SpotPositionService;
import com.exchange_simulator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AdminController {
    final private UserService userService;
    final private OrderService orderService;
    final private SpotPositionService spotPositionService;
    final private UserMapper userMapper;
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserResponseDto>> getUsers(){
        return ResponseEntity.ok(userService
                .getUsers()
                .stream()
                .map(userMapper::toDto)
                .toList());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable("id") Long id
    ){
        var user = userService.getUserById(id);
        return user
                .map(u -> ResponseEntity.ok().body(userMapper.toDto(u)))
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDto>> getUsersOrders(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(orderService.getUserOrders(id));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/positions")
    public ResponseEntity<List<SpotPositionResponseDto>> getUsersPositions(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(spotPositionService.getPortfolio(id));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/funds")
    public ResponseEntity<UserResponseDto> updateUsersFunds(
            @PathVariable("id") Long id,
            @RequestBody BigDecimal amount
    ){
        var user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userService.updateFunds(user, amount);

        return ResponseEntity.ok(userMapper.toDto(user));
    }
}
