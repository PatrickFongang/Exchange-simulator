package com.exchange_simulator.service;

import com.exchange_simulator.dto.binance.MarkPriceStreamEvent;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class LimitOrderService extends OrderService {
    CryptoWebSocketService cryptoWebSocketService;

    record QueuePair(PriorityQueue<Order> buy, PriorityQueue<Order> sell) {}
    HashMap<String, QueuePair> orderQueues = new HashMap<>();

    public LimitOrderService(OrderRepository orderRepository,
                              UserRepository userRepository,
                              CryptoDataService cryptoDataService,
                              SpotPositionService spotPositionService,
                              CryptoWebSocketService cryptoWebSocketService
                             )
    {
        // fetch db into order queues

        this.cryptoWebSocketService = cryptoWebSocketService;
        super(orderRepository, userRepository, cryptoDataService, spotPositionService);
    }

    private void addToQueue(Order order){
        if(!orderQueues.containsKey(order.getToken())){
            var queueBuy = new PriorityQueue<>(Comparator.comparing(Order::getTokenPrice).reversed());
            var queueSell = new PriorityQueue<>(Comparator.comparing(Order::getTokenPrice));

            orderQueues.put(order.getToken(), new QueuePair(queueBuy, queueSell));
            cryptoWebSocketService.AddTokenListener(order.getToken(), this::handleWatcherEvent);
        }

        if(order.getTransactionType() == TransactionType.BUY){
            orderQueues.get(order.getToken()).buy.add(order);
        }
        else{
            orderQueues.get(order.getToken()).sell.add(order);
        }
    }

    public void handleWatcherEvent(MarkPriceStreamEvent event){
        if(orderQueues.containsKey(event.symbol())) return;

        var price = event.indexPrice();

        var buyQueue = orderQueues.get(event.symbol()).buy;
        var sellQueue = orderQueues.get(event.symbol()).sell;

        while(!buyQueue.isEmpty() && buyQueue.peek().getTokenPrice().compareTo(price) >= 0){
            var order = buyQueue.poll();

            // optional funds return
            order.setClosedAt(Instant.now());
            orderRepository.save(order);
        }

        while(!sellQueue.isEmpty() && sellQueue.peek().getTokenPrice().compareTo(price) <= 0){
            var order = sellQueue.poll();

            // optional funds return
            order.setClosedAt(Instant.now());
            orderRepository.save(order);
        }

        if(buyQueue.isEmpty() && sellQueue.isEmpty()){
            // remove socket listener
        }
    }

    @Transactional
    public Order buy(OrderRequestDto dto) {
        var data = prepareToBuy(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        user.setFunds(user.getFunds().subtract(orderValue));
        var newOrder = new Order(dto.getToken(), dto.getQuantity(), tokenPrice, orderValue, user, TransactionType.BUY, OrderType.LIMIT, null);

        addToQueue(newOrder);

        return orderRepository.save(newOrder);
    }

    @Transactional
    public Order sell(OrderRequestDto dto) {
        var data = prepareToSell(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        spotPositionService.handleSell(data.user(), dto, data.tokenPrice());

        var newOrder = new Order(dto.getToken(), dto.getQuantity(), tokenPrice, orderValue, user, TransactionType.SELL, OrderType.LIMIT, null);

        addToQueue(newOrder);

        return orderRepository.save(newOrder);
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
