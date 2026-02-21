package com.exchange_simulator.controller;

import com.exchange_simulator.mapper.OrderMapper;
import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.security.CustomUserDetails;
import com.exchange_simulator.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders/market")
@RequiredArgsConstructor
public class MarketOrderController {
    private final MarketOrderService marketOrderService;
    private final OrderMapper orderMapper;
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(marketOrderService.getUserMarketOrders(user.getId()));
    }
    @GetMapping("/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(marketOrderService.getUserBuyMarketOrders(user.getId()));
    }
    @GetMapping("/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(marketOrderService.getUserSellMarketOrders(user.getId()));
    }
    @PostMapping("/buy")
    public ResponseEntity<OrderResponseDto> buy(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        var order = marketOrderService.buy(orderRequestDto, user.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }
    @PostMapping("/sell")
    public ResponseEntity<OrderResponseDto> sell(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        var order = marketOrderService.sell(orderRequestDto, user.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }
}
