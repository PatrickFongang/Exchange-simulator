package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.security.CustomUserDetails;
import com.exchange_simulator.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @GetMapping()
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(orderService.getUserOrders(user.getId()));
    }
    @GetMapping("/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(orderService.getUserBuyOrders(user.getId()));
    }
    @GetMapping("/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(orderService.getUserSellOrders(user.getId()));
    }
}
