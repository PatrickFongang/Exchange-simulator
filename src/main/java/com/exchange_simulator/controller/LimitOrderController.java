package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.enums.TransactionType;
import com.exchange_simulator.exceptionHandler.exceptions.OrderNotFoundException;
import com.exchange_simulator.service.LimitOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class LimitOrderController {
    private final LimitOrderService limitOrderService;

    @GetMapping("/{userId}/limit")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(limitOrderService.getUserLimitOrders(userId));
    }
    @GetMapping("/{userId}/limit/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(limitOrderService.getUserBuyLimitOrders(userId));
    }
    @GetMapping("/{userId}/limit/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(limitOrderService.getUserSellLimitOrders(userId));
    }
    @PostMapping("/{userId}/limit/buy")
    public ResponseEntity<OrderResponseDto> buy(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto orderRequestDto
    ){
        orderRequestDto.setUserId(userId);
        var order = limitOrderService.buy(orderRequestDto);
        return ResponseEntity.ok(limitOrderService.getDto(order));
    }

    @PostMapping("/{userId}/limit/sell")
    public ResponseEntity<OrderResponseDto> sell(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto orderRequestDto
    ){
        orderRequestDto.setUserId(userId);
        var order = limitOrderService.sell(orderRequestDto);
        return ResponseEntity.ok(limitOrderService.getDto(order));
    }
    @DeleteMapping("/{userId}/limit/cancel/{orderId}")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId
    ){
        var order = limitOrderService.findByUserAndOrderId(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if(order.getTransactionType() == TransactionType.BUY)
            limitOrderService.cancelBuyOrder(order);
        else
            limitOrderService.cancelSellOrder(order);

        return ResponseEntity.ok(limitOrderService.getDto(order));
    }
}
