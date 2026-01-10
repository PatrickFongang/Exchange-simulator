package com.exchange_simulator.service;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.entity.Order;
import com.exchange_simulator.enums.OrderType;
import com.exchange_simulator.enums.TransactionType;
import com.exchange_simulator.repository.OrderRepository;
import com.exchange_simulator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LimitOrderService extends OrderService {
    public LimitOrderService(OrderRepository orderRepository,
                              UserRepository userRepository,
                              CryptoDataService cryptoDataService,
                              SpotPositionService spotPositionService)
    { super(orderRepository, userRepository, cryptoDataService, spotPositionService); }
    @Transactional
    public Order buy(OrderRequestDto dto) {
        var data = prepareToBuy(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        /*
            LimitOrder Logic
        */

        user.setFunds(user.getFunds().subtract(orderValue));
        return orderRepository.save(new Order(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, TransactionType.BUY, OrderType.LIMIT, null));
    }

    @Transactional
    public Order sell(OrderRequestDto dto) {
        var data = prepareToSell(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        /*
            LimitOrder Logic
         */
        spotPositionService.handleSell(data.user(), dto, data.tokenPrice());

        return orderRepository.save(new Order(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, TransactionType.SELL, OrderType.LIMIT, null));
    }

    public List<OrderResponseDto> getUserLimitOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByUserId(userId)
                .stream()
                .filter(order -> order.getOrderType().equals(OrderType.LIMIT))
                .map(this::getDto)
                .toList();
    }
    public List<OrderResponseDto> getUserBuyLimitOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByOrderTypeAndUserId(TransactionType.BUY, userId)
                .stream()
                .filter(order -> order.getOrderType().equals(OrderType.LIMIT))
                .map(this::getDto)
                .toList();
    }
    public List<OrderResponseDto> getUserSellLimitOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByOrderTypeAndUserId(TransactionType.SELL,userId)
                .stream()
                .filter(order -> order.getOrderType().equals(OrderType.LIMIT))
                .map(this::getDto)
                .toList();
    }
    /*
        TODO: canceLimitOrder method, processPendingLimitOrders method
     */
}
