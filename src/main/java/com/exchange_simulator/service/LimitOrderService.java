package com.exchange_simulator.service;

import com.exchange_simulator.dto.binance.MarkPriceStreamEvent;
import com.exchange_simulator.dto.order.OrderRequestDto;
import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.entity.Order;
import com.exchange_simulator.enums.OrderType;
import com.exchange_simulator.enums.TransactionType;
import com.exchange_simulator.repository.OrderRepository;
import com.exchange_simulator.repository.SpotPositionRepository;
import com.exchange_simulator.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class LimitOrderService extends OrderService {
    private final SpotPositionRepository spotPositionRepository;
    CryptoWebSocketService cryptoWebSocketService;

    record QueuePair(PriorityBlockingQueue<Order> buy, PriorityBlockingQueue<Order> sell) {}
    Map<String, QueuePair> orderQueues = new ConcurrentHashMap<>();
    Set<Long> cancelledOrders = ConcurrentHashMap.newKeySet();
    Map<String, Consumer<Runnable>> listenerCleaners = new ConcurrentHashMap<>();

    public LimitOrderService(OrderRepository orderRepository,
                              UserRepository userRepository,
                              CryptoDataService cryptoDataService,
                              SpotPositionService spotPositionService,
                              CryptoWebSocketService cryptoWebSocketService,
                             SpotPositionRepository spotPositionRepository)
    {
        // fetch db into order queues

        this.cryptoWebSocketService = cryptoWebSocketService;
        super(orderRepository, userRepository, cryptoDataService, spotPositionService);
        this.spotPositionRepository = spotPositionRepository;

        syncOrdersToQueue();
    }

    public Stream<Order> getBuyActiveOrdersQueue(String symbol){
        if(orderQueues.containsKey(symbol)){
            return orderQueues.get(symbol).buy.stream()
                    .filter(o -> !cancelledOrders.contains(o.getId()));
        }
        return Stream.empty();
    }

    public Stream<Order> getSellActiveOrdersQueue(String symbol){
        if(orderQueues.containsKey(symbol)){
            return orderQueues.get(symbol).sell.stream()
                    .filter(o -> !cancelledOrders.contains(o.getId()));
        }
        return Stream.empty();
    }

    private void syncOrdersToQueue(){
        var orders = orderRepository.findByClosedAtIsNull();
        for(var order : orders){
            addToQueue(order);
        }
    }

    private void addToQueue(Order order){
        if(!orderQueues.containsKey(order.getToken())){
            var queueBuy = new PriorityBlockingQueue<>(2, Comparator.comparing(Order::getTokenPrice).reversed());
            var queueSell = new PriorityBlockingQueue<>(2, Comparator.comparing(Order::getTokenPrice));

            orderQueues.put(order.getToken(), new QueuePair(queueBuy, queueSell));
        }

        if(!listenerCleaners.containsKey(order.getToken())){
            listenerCleaners.put(order.getToken(),
                this.cryptoWebSocketService.AddTokenListener(order.getToken(), this::handleWatcherEvent));
        }

        if(order.getTransactionType() == TransactionType.BUY){
            orderQueues.get(order.getToken()).buy.add(order);
        }
        else{
            orderQueues.get(order.getToken()).sell.add(order);
        }
    }

    @EventListener
    @Async
    @Transactional
    public void handleWatcherEvent(MarkPriceStreamEvent event){
        System.out.println(event.getSymbol() + " -> " + event.getIndexPrice());
        if(!orderQueues.containsKey(event.getSymbol())) return;

        var price = event.getIndexPrice();

        var buyQueue = orderQueues.get(event.getSymbol()).buy;
        var sellQueue = orderQueues.get(event.getSymbol()).sell;

        while(!buyQueue.isEmpty() && buyQueue.peek().getTokenPrice().compareTo(price) >= 0){
            var order = buyQueue.poll();

            if(!cancelledOrders.contains(order.getId())) {
                System.out.println("Filling buy order " + order.getId());
                this.finalizeBuyOrder(order);
            }else cancelledOrders.remove(order.getId());
        }

        while(!sellQueue.isEmpty() && sellQueue.peek().getTokenPrice().compareTo(price) <= 0){
            var order = sellQueue.poll();

            if(!cancelledOrders.contains(order.getId())) {
                System.out.println("Filling sell order " + order.getId());
                this.finalizeSellOrder(order);
            }else cancelledOrders.remove(order.getId());
        }

        while(!buyQueue.isEmpty() && cancelledOrders.contains(buyQueue.peek().getId()))
            buyQueue.poll();

        while(!sellQueue.isEmpty() && cancelledOrders.contains(sellQueue.peek().getId()))
            sellQueue.poll();

        if(buyQueue.isEmpty() && sellQueue.isEmpty() && listenerCleaners.containsKey(event.getSymbol())){
            listenerCleaners.get(event.getSymbol()).accept(() ->
                    listenerCleaners.remove(event.getSymbol())
            );
        }
    }

    @Transactional
    public Order buy(OrderRequestDto dto) {
        var data = prepareToBuy(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        user.setFunds(user.getFunds().subtract(orderValue));
        var newOrder = new Order(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, TransactionType.BUY, OrderType.LIMIT, null);

        addToQueue(newOrder);

        return orderRepository.save(newOrder);
    }

    @Transactional
    public Order sell(OrderRequestDto dto) {
        var data = prepareToSell(dto);
        var user = data.user();
        var orderValue = data.orderValue();
        var tokenPrice = data.tokenPrice();

        var newOrder = new Order(dto.getToken(), dto.getQuantity(), tokenPrice,
                orderValue, user, TransactionType.SELL, OrderType.LIMIT, null);
        spotPositionService.handleSell(newOrder);

        addToQueue(newOrder);

        return orderRepository.save(newOrder);
    }

    void finalizeBuyOrder(Order order){
        order.setClosedAt(Instant.now());
        orderRepository.saveAndFlush(order);

        spotPositionService.handleBuy(order);
    }

    void finalizeSellOrder(Order order){
        order.setClosedAt(Instant.now());

        var user = order.getUser();
        user.setFunds(user.getFunds().add(order.getOrderValue()));

        orderRepository.save(order);

        spotPositionService.deletePosition(order.getUser(), order.getToken());
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

    @Transactional
    public void cancelSellOrder(Order order)
    {
        cancelledOrders.add(order.getId());

        var position = spotPositionService.findPositionByToken(order.getUser(), order.getToken()).get();
        position.setQuantity(position.getQuantity().add(order.getQuantity()));
        spotPositionRepository.save(position);

        orderRepository.delete(order);
        for(var i : cancelledOrders)
            System.out.println(i);
    }
    @Transactional
    public void cancelBuyOrder(Order order)
    {
        cancelledOrders.add(order.getId());

        var user = order.getUser();
        user.setFunds(user.getFunds().add(order.getOrderValue()));
        userRepository.save(user);


        orderRepository.delete(order);
        for(var i : cancelledOrders)
            System.out.println(i);
    }
}
