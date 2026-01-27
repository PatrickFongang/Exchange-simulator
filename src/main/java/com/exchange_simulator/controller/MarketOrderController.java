package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class MarketOrderController {
    private final MarketOrderService marketOrderService;
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/market")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(marketOrderService.getUserMarketOrders(userId));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/market/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(marketOrderService.getUserBuyMarketOrders(userId));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/market/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(marketOrderService.getUserSellMarketOrders(userId));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/{userId}/market/buy")
    public ResponseEntity<OrderResponseDto> buy(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto orderRequestDto
    ){
        orderRequestDto.setUserId(userId);
        var order = marketOrderService.buy(orderRequestDto);
        return ResponseEntity.ok(marketOrderService.getDto(order));
    }
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/{userId}/market/sell")
    public ResponseEntity<OrderResponseDto> sell(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto orderRequestDto
    ){
        orderRequestDto.setUserId(userId);
        var order = marketOrderService.sell(orderRequestDto);
        return ResponseEntity.ok(marketOrderService.getDto(order));
    }
}
