package com.exchange_simulator.controller;

import com.exchange_simulator.dto.marketOrder.MarketOrderRequestDto;
import com.exchange_simulator.dto.marketOrder.MarketOrderResponseDto;
import com.exchange_simulator.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class MarketOrderController {
    private final MarketOrderService marketOrderService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<MarketOrderResponseDto>> getUserOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(marketOrderService.getUserOrders(userId));
    }
    @GetMapping("/{userId}/buy")
    public ResponseEntity<List<MarketOrderResponseDto>> getUserBuyOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(marketOrderService.getUserBuyOrders(userId));
    }
    @GetMapping("/{userId}/sell")
    public ResponseEntity<List<MarketOrderResponseDto>> getUserSellOrders(@PathVariable Long userId)
    {
        return ResponseEntity.ok(marketOrderService.getUserSellOrders(userId));
    }
    @PostMapping("/{userId}/buy")
    public ResponseEntity<MarketOrderResponseDto> buy(
            @PathVariable Long userId,
            @RequestBody MarketOrderRequestDto marketOrderRequestDto
    ){
        marketOrderRequestDto.setUserId(userId);
        var order = marketOrderService.buy(marketOrderRequestDto);
        return ResponseEntity.ok(marketOrderService.getDto(order));
    }
    @PostMapping("/{userId}/sell")
    public ResponseEntity<MarketOrderResponseDto> sell(
            @PathVariable Long userId,
            @RequestBody MarketOrderRequestDto marketOrderRequestDto
    ){
        marketOrderRequestDto.setUserId(userId);
        var order = marketOrderService.sell(marketOrderRequestDto);
        return ResponseEntity.ok(marketOrderService.getDto(order));
    }
}
