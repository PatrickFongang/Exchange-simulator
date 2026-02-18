package com.exchange_simulator.service;

import com.exchange_simulator.Mapper.SpotPositionMapper;
import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.entity.Order;
import com.exchange_simulator.entity.SpotPosition;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.NotEnoughResourcesException;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.SpotPositionNotFoundException;
import com.exchange_simulator.repository.OrderRepository;
import com.exchange_simulator.repository.SpotPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpotPositionService {
    private final SpotPositionRepository spotPositionRepository;
    private final OrderRepository orderRepository;
    private final SpotPositionMapper spotPositionMapper;

    public void handleBuy(Order order) {

        var position = handlePosition(order.getToken(), order.getQuantity(), order.getTokenPrice(), order.getUser());

        spotPositionRepository.saveAndFlush(position);

        spotPositionRepository.updateAvgBuyPriceByUserAndPositionId(order.getUser().getId(), position.getId(), order.getToken());
    }

    public void handleSell(Order order) {
        var position = findPositionByToken(order.getUser(), order.getToken());
        if(position.isEmpty())
            throw new SpotPositionNotFoundException(order.getUser(), order.getToken());

        var ownedTokens = position.get().getQuantity();
        validateResources(ownedTokens, order.getQuantity());

        ownedTokens = ownedTokens.subtract(order.getQuantity());
        position.get().setQuantity(ownedTokens);

        if(syncPositionExist(position.get())){
            spotPositionRepository.save(position.get());
        }
    }

    public List<SpotPositionResponseDto> getPortfolio(Long userId) {
        return spotPositionRepository.findAllByUserId(userId)
                .stream()
                .map(spotPositionMapper::toDto)
                .toList();
    }
    public Optional<SpotPosition> findPositionByToken(User user, String token) {
        var positions = spotPositionRepository.findAllByUserIdWithLock(user.getId());
        return positions.stream().filter(p -> p.getToken().equals(token)).findFirst();
    }

    public Optional<SpotPosition> findPositionByTokenUnlocked(Long userId, String token) {
        return spotPositionRepository.findByUserIdAndToken(userId, token);
    }

    public boolean syncPositionExist(SpotPosition position){
        if(position.getQuantity().compareTo(BigDecimal.ZERO) > 0){
            return true;
        }

        var openSell = orderRepository
                .findOpenSellOrdersByUserAndToken(
                        position.getUser().getId(),
                        position.getToken());

        if(openSell.isEmpty()){
            spotPositionRepository.delete(position);
            return false;
        }

        return true;
    }

    private SpotPosition handlePosition(String token, BigDecimal quantity, BigDecimal tokenPrice, User user) {
        Optional<SpotPosition> position = findPositionByToken(user, token);

        position.ifPresent(pos ->
                    pos.setQuantity(pos.getQuantity().add(quantity))
        );
        Instant lastBuyOrder = orderRepository.getNewestOrderTimestamp(user.getId(), token);
        return position.orElseGet(() ->
                spotPositionRepository.save(new SpotPosition(user, token, quantity, tokenPrice, lastBuyOrder))
        );
    }
    private void validateResources(BigDecimal owned, BigDecimal order){
        if(owned.compareTo(order) < 0)
            throw new NotEnoughResourcesException(order, owned);
    }
}
