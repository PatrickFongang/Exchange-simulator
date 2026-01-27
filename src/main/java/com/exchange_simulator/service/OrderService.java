package com.exchange_simulator.service;

import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.entity.Order;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.enums.TransactionType;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.BadQuantityException;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.InsufficientFundsException;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserNotFoundException;
import com.exchange_simulator.repository.OrderRepository;
import com.exchange_simulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    protected final OrderRepository orderRepository;
    protected final UserRepository userRepository;
    protected final CryptoDataService cryptoDataService;
    protected final SpotPositionService spotPositionService;

    protected OrderFinalization prepareToBuy(OrderRequestDto dto){
        var user = findUserByIdWithLock(dto.getUserId());
        validateQuantity(dto.getQuantity());

        var tokenPrice = dto.getLimit() == null ? cryptoDataService.getPrice(dto.getToken()) : dto.getLimit();
        var orderValue = toPay(user, dto.getQuantity(), tokenPrice);

        return new OrderFinalization(user, orderValue, tokenPrice);
    }

    protected OrderFinalization prepareToSell(OrderRequestDto dto){
        var user = findUserByIdWithLock(dto.getUserId());
        validateQuantity(dto.getQuantity());

        var tokenPrice = dto.getLimit() == null ? cryptoDataService.getPrice(dto.getToken()) : dto.getLimit();
        var orderValue = tokenPrice.multiply(dto.getQuantity());

        return new OrderFinalization(user, orderValue, tokenPrice);
    }
    public List<OrderResponseDto> getUserOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(this::getDto)
                .toList();
    }
    public List<OrderResponseDto> getUserBuyOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByOrderTypeAndUserId(TransactionType.BUY,userId)
                .stream()
                .map(this::getDto)
                .toList();
    }
    public List<OrderResponseDto> getUserSellOrders(Long userId)
    {
        findUserById(userId);
        return orderRepository.findAllByOrderTypeAndUserId(TransactionType.SELL,userId)
                .stream()
                .map(this::getDto)
                .toList();
    }

    public OrderResponseDto getDto(Order order){
        var tokenPrice = order.getTokenPrice();
        var orderValue = tokenPrice.multiply(order.getQuantity());
        return new OrderResponseDto(
                order.getUser().getId(),
                order.getId(),
                order.getCreatedAt(),
                order.getToken(),
                order.getQuantity(),
                order.getTokenPrice(),
                orderValue,
                order.getTransactionType(),
                order.getOrderType(),
                order.getClosedAt()
        );
    }
    public Optional<Order> findByUserAndOrderId(Long userId, Long orderId){
        return orderRepository.findByOrderAndUserId(userId, orderId);
    }
    protected void validateQuantity(BigDecimal quantity){
        if(quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadQuantityException(quantity);
    }
    protected BigDecimal toPay(User user, BigDecimal quantity, BigDecimal tokenPrice) {
        var price = tokenPrice.multiply(quantity);
        if(user.getFunds().compareTo(price) < 0)
            throw new InsufficientFundsException(user.getFunds(), price);
        return price;
    }
    protected User findUserByIdWithLock(Long userId) {
        return userRepository.findByIdWithLock(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    protected User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    protected record OrderFinalization(User user, BigDecimal orderValue, BigDecimal tokenPrice) {}
}
