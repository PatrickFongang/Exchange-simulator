package com.exchange_simulator.controller;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.enums.TransactionType;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.OrderNotFoundException;
import com.exchange_simulator.security.CustomUserDetails;
import com.exchange_simulator.service.LimitOrderService;
import com.exchange_simulator.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-orders")
@RequiredArgsConstructor
public class LimitOrderController {
    private final LimitOrderService limitOrderService;
    private final OrderService orderService;

    @GetMapping("/limit")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(limitOrderService.getUserLimitOrders(user.getId()));
    }
    @GetMapping("/limit/buy")
    public ResponseEntity<List<OrderResponseDto>> getUserBuyOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(limitOrderService.getUserBuyLimitOrders(user.getId()));
    }

    @GetMapping("/limit/sell")
    public ResponseEntity<List<OrderResponseDto>> getUserSellOrders(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(limitOrderService.getUserSellLimitOrders(user.getId()));
    }
    @PostMapping("/limit/buy")
    public ResponseEntity<OrderResponseDto> buy(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        orderRequestDto.setUserId(user.getId());
        var order = limitOrderService.buy(orderRequestDto);
        return ResponseEntity.ok(limitOrderService.getDto(order));
    }
    @PostMapping("/limit/sell")
    public ResponseEntity<OrderResponseDto> sell(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomUserDetails user){
        orderRequestDto.setUserId(user.getId());
        var order = limitOrderService.sell(orderRequestDto);
        return ResponseEntity.ok(limitOrderService.getDto(order));
    }
    @DeleteMapping("/limit/{orderId}")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        var order = limitOrderService.findByUserAndOrderId(user.getId(), orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if(order.getTransactionType() == TransactionType.BUY)
            limitOrderService.cancelBuyOrder(order);
        else
            limitOrderService.cancelSellOrder(order);

        return ResponseEntity.ok(limitOrderService.getDto(order));
    }

    @GetMapping("/book/{token}/buy")
    public ResponseEntity<List<OrderResponseDto>> buyOrderBook(
            @PathVariable String token
    ){
        return ResponseEntity.ok(
                limitOrderService.getBuyActiveOrdersQueue(token).map(orderService::getDto).toList()
        );
    }

    @GetMapping("/book/{token}/sell")
    public ResponseEntity<List<OrderResponseDto>> sellOrderBook(
            @PathVariable String token
    ){
        return ResponseEntity.ok(
                limitOrderService.getSellActiveOrdersQueue(token).map(orderService::getDto).toList()
        );
    }

}
