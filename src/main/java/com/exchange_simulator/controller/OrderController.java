package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
    @GetMapping("/{userId}/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(orderService.getUserBuyOrders(userId));
    }
    @GetMapping("/{userId}/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(orderService.getUserSellOrders(userId));
    }
}
