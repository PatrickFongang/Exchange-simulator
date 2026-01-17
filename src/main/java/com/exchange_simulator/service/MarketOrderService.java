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

import java.time.Instant;
import java.util.List;

@Service
public class MarketOrderService extends OrderService {
    public MarketOrderService(OrderRepository orderRepository,
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

        var order = new Order(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, TransactionType.BUY, OrderType.MARKET, Instant.now());

        spotPositionService.handleBuy(order);

        user.setFunds(user.getFunds().subtract(orderValue));
        return orderRepository.save(order);
    }

    @Transactional
    public Order sell(OrderRequestDto dto) {
        var data = prepareToSell(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        var order = new Order(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, TransactionType.SELL, OrderType.MARKET, Instant.now());

        spotPositionService.handleSell(order);

        user.setFunds(user.getFunds().add(orderValue));
        return orderRepository.save(order);
    }

    public List<OrderResponseDto> getUserMarketOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByUserId(userId)
                .stream()
                .filter(order -> order.getOrderType().equals(OrderType.MARKET))
                .map(this::getDto)
                .toList();
    }
    public List<OrderResponseDto> getUserBuyMarketOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByOrderTypeAndUserId(TransactionType.BUY, userId)
                .stream()
                .filter(order -> order.getOrderType().equals(OrderType.MARKET))
                .map(this::getDto)
                .toList();
    }
    public List<OrderResponseDto> getUserSellMarketOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByOrderTypeAndUserId(TransactionType.SELL,userId)
                .stream()
                .filter(order -> order.getOrderType().equals(OrderType.MARKET))
                .map(this::getDto)
                .toList();
    }
}
