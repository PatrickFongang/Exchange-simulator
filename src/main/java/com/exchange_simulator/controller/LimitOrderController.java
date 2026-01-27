package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.enums.TransactionType;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.OrderNotFoundException;
import com.exchange_simulator.service.LimitOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class LimitOrderController {
    private final LimitOrderService limitOrderService;

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/limit")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(limitOrderService.getUserLimitOrders(userId));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/limit/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(limitOrderService.getUserBuyLimitOrders(userId));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/limit/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(limitOrderService.getUserSellLimitOrders(userId));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/{userId}/limit/buy")
    public ResponseEntity<OrderResponseDto> buy(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto orderRequestDto
    ){
        orderRequestDto.setUserId(userId);
        var order = limitOrderService.buy(orderRequestDto);
        return ResponseEntity.ok(limitOrderService.getDto(order));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/{userId}/limit/sell")
    public ResponseEntity<OrderResponseDto> sell(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto orderRequestDto
    ){
        orderRequestDto.setUserId(userId);
        var order = limitOrderService.sell(orderRequestDto);
        return ResponseEntity.ok(limitOrderService.getDto(order));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
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
