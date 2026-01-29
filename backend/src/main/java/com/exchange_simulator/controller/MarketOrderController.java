package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.security.CustomUserDetails;
import com.exchange_simulator.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class MarketOrderController {
    private final MarketOrderService marketOrderService;
    @GetMapping("/market")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(marketOrderService.getUserMarketOrders(user.getId()));
    }
    @GetMapping("/market/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(marketOrderService.getUserBuyMarketOrders(user.getId()));
    }
    @GetMapping("/market/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(marketOrderService.getUserSellMarketOrders(user.getId()));
    }
    @PostMapping("/market/buy")
    public ResponseEntity<OrderResponseDto> buy(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        orderRequestDto.setUserId(user.getId());
        var order = marketOrderService.buy(orderRequestDto);
        return ResponseEntity.ok(marketOrderService.getDto(order));
    }
    @PostMapping("/market/sell")
    public ResponseEntity<OrderResponseDto> sell(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        orderRequestDto.setUserId(user.getId());
        var order = marketOrderService.sell(orderRequestDto);
        return ResponseEntity.ok(marketOrderService.getDto(order));
    }
}
