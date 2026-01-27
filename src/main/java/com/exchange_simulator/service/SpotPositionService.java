package com.exchange_simulator.service;

import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.entity.Order;
import com.exchange_simulator.entity.SpotPosition;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.enums.OrderType;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.NotEnoughResourcesException;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.SpotPositionNotFoundException;
import com.exchange_simulator.repository.OrderRepository;
import com.exchange_simulator.repository.SpotPositionRepository;
import com.exchange_simulator.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;
    private final CryptoDataService cryptoDataService;

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

        if (ownedTokens.compareTo(BigDecimal.ZERO) == 0 && order.getOrderType() == OrderType.MARKET) {
            spotPositionRepository.delete(position.get());
        } else {
            spotPositionRepository.save(position.get());
        }
    }

    public List<SpotPositionResponseDto> getPortfolio(Long userId) {
        return spotPositionRepository.findAllByUserId(userId)
                .stream()
                .map(this::getDto)
                .toList();
    }

    public SpotPositionResponseDto getDto(SpotPosition position){
        var tokenPrice = cryptoDataService.getPrice(position.getToken());
        return new SpotPositionResponseDto(
                position.getId(),
                position.getToken(),
                position.getQuantity(),
                position.getAvgBuyPrice(),
                tokenPrice.multiply(position.getQuantity()),
                position.getTimestamp()
        );
    }

    public Optional<SpotPosition> findPositionByToken(User user, String token) {
        var positions = spotPositionRepository.findAllByUserIdWithLock(user.getId());
        return positions.stream().filter(p -> p.getToken().equals(token)).findFirst();
    }

    @Transactional
    public void deletePosition(User user, String token) {
        var position = findPositionByToken(user, token).get();
        spotPositionRepository.delete(position);
    }
    private SpotPosition handlePosition(String token, BigDecimal quantity, BigDecimal tokenPrice, User user) {
        Optional<SpotPosition> position = findPositionByToken(user, token);

        position.ifPresent(pos ->
                    pos.setQuantity(pos.getQuantity().add(quantity))
        );
        Instant lastBuyOrder = orderRepository.getNewestOrderTimestamp(user.getId(), token);
        return position.orElseGet(() ->
                spotPositionRepository.save(new SpotPosition(token, quantity, tokenPrice, user, lastBuyOrder))
        );
    }
    private void validateResources(BigDecimal owned, BigDecimal order){
        if(owned.compareTo(order) < 0)
            throw new NotEnoughResourcesException(order, owned);
    }
}
