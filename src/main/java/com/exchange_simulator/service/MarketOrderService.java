package com.exchange_simulator.service;

import com.exchange_simulator.dto.marketOrder.MarketOrderRequestDto;
import com.exchange_simulator.dto.marketOrder.MarketOrderResponseDto;
import com.exchange_simulator.entity.MarketOrder;
import com.exchange_simulator.entity.SpotPosition;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.enums.OrderType;
import com.exchange_simulator.exceptionHandler.exceptions.BadQuantityException;
import com.exchange_simulator.exceptionHandler.exceptions.InsufficientFundsException;
import com.exchange_simulator.exceptionHandler.exceptions.NotEnoughResourcesException;
import com.exchange_simulator.exceptionHandler.exceptions.UserNotFoundException;
import com.exchange_simulator.repository.MarketOrderRepository;
import com.exchange_simulator.repository.SpotPositionRepository;
import com.exchange_simulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.knowm.xchange.dto.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketOrderService {
    private final MarketOrderRepository marketOrderRepository;
    private final UserRepository userRepository;
    private final CryptoDataService cryptoDataService;
    private final SpotPositionService spotPositionService;

    @Transactional
    public MarketOrder buy(MarketOrderRequestDto dto) {
        var user = findUserByIdWithLock(dto.getUserId());
        validateQuantity(dto.getQuantity());

        var tokenPrice = cryptoDataService.getPrice(dto.getToken());
        var transactionPrice = toPay(user, dto.getQuantity(), tokenPrice);
        var order = marketOrderRepository.save(new MarketOrder(dto.getToken(), dto.getQuantity(), tokenPrice,
                transactionPrice, user, OrderType.BUY));

        spotPositionService.handleBuy(user, dto, tokenPrice);

        user.setFunds(user.getFunds().subtract(transactionPrice));
        return order;
    }

    @Transactional
    public MarketOrder sell(MarketOrderRequestDto dto) {
        var user = findUserByIdWithLock(dto.getUserId());
        validateQuantity(dto.getQuantity());

        var tokenPrice = cryptoDataService.getPrice(dto.getToken());
        var orderValue = tokenPrice.multiply(dto.getQuantity());

        spotPositionService.handleSell(user, dto, tokenPrice);

        user.setFunds(user.getFunds().add(orderValue));
        return marketOrderRepository.save(new MarketOrder(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, OrderType.SELL));
    }

    public List<MarketOrderResponseDto> getUserOrders(Long userId)
    {
        findUserById(userId);
        return marketOrderRepository.findAllByUserId(userId)
                .stream()
                .map(this::getDto)
                .toList();
    }
    public List<MarketOrderResponseDto> getUserBuyOrders(Long userId)
    {
        findUserById(userId);
        return marketOrderRepository.findAllByOrderTypeAndUserId(OrderType.BUY,userId)
                .stream()
                .map(this::getDto)
                .toList();
    }
    public List<MarketOrderResponseDto> getUserSellOrders(Long userId)
    {
        findUserById(userId);
        return marketOrderRepository.findAllByOrderTypeAndUserId(OrderType.SELL,userId)
                .stream()
                .map(this::getDto)
                .toList();
    }

    public MarketOrderResponseDto getDto(MarketOrder marketOrder){
        var tokenPrice = marketOrder.getTokenPrice();
        var orderValue = tokenPrice.multiply(marketOrder.getQuantity());
        return new MarketOrderResponseDto(
                marketOrder.getUser().getId(),
                marketOrder.getId(),
                marketOrder.getCreatedAt(),
                marketOrder.getToken(),
                marketOrder.getQuantity(),
                marketOrder.getTokenPrice(),
                orderValue,
                marketOrder.getOrderType()
        );
    }
    private void validateQuantity(BigDecimal quantity){
        if(quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadQuantityException(quantity);
    }
    private BigDecimal toPay(User user, BigDecimal quantity, BigDecimal tokenPrice) {
        var price = tokenPrice.multiply(quantity);
        if(user.getFunds().compareTo(price) < 0)
            throw new InsufficientFundsException(price, user.getFunds());
        return price;
    }
    private User findUserByIdWithLock(Long userId) {
        return userRepository.findByIdWithLock(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
