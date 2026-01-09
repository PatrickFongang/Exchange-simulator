package com.exchange_simulator.service;

import com.exchange_simulator.dto.marketOrder.MarketOrderRequestDto;
import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.entity.SpotPosition;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.exceptionHandler.exceptions.BadQuantityException;
import com.exchange_simulator.exceptionHandler.exceptions.NotEnoughResourcesException;
import com.exchange_simulator.exceptionHandler.exceptions.SpotPositionNotFoundException;
import com.exchange_simulator.exceptionHandler.exceptions.UserNotFoundException;
import com.exchange_simulator.repository.MarketOrderRepository;
import com.exchange_simulator.repository.SpotPositionRepository;
import com.exchange_simulator.repository.UserRepository;
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
    private final MarketOrderRepository marketOrderRepository;
    private final UserRepository userRepository;
    private final CryptoDataService cryptoDataService;

    public void handleBuy(User user, MarketOrderRequestDto dto, BigDecimal tokenPrice) {
        var position = handlePosition(dto.getToken(), dto.getQuantity(), tokenPrice, user);

        position.setPositionValue(position.getQuantity().multiply(tokenPrice));
        spotPositionRepository.save(position);

        spotPositionRepository.updateAvgBuyPriceByUserAndPositionId(user.getId(), position.getId(), dto.getToken());
    }

    public void handleSell(User user, MarketOrderRequestDto dto, BigDecimal tokenPrice) {
        var position = findPositionByToken(user, dto.getToken());
        if(position.isEmpty())
            throw new SpotPositionNotFoundException(user,  dto.getToken());

        var ownedTokens = position.get().getQuantity();
        validateResources(ownedTokens, dto.getQuantity());

        ownedTokens = ownedTokens.subtract(dto.getQuantity());
        position.get().setQuantity(ownedTokens);

        if (ownedTokens.compareTo(BigDecimal.ZERO) == 0) {
            spotPositionRepository.delete(position.get());
        } else {
            position.get().setPositionValue(ownedTokens.multiply(tokenPrice));
            spotPositionRepository.save(position.get());
        }
    }

    public List<SpotPositionResponseDto> getPortfolio(Long userId) {
        return spotPositionRepository.findAllByUserId(userId)
                .stream()
                .map(SpotPositionService::getDto)
                .toList();
    }

    public static SpotPositionResponseDto getDto(SpotPosition position){
        return new SpotPositionResponseDto(
                position.getId(),
                position.getToken(),
                position.getQuantity(),
                position.getAvgBuyPrice(),
                position.getTimestamp()
        );
    }

    private Optional<SpotPosition> findPositionByToken(User user, String token) {
        var positions = spotPositionRepository.findAllByUserIdWithLock(user.getId());
        return positions.stream().filter(p -> p.getToken().equals(token)).findFirst();
    }
    private SpotPosition handlePosition(String token, BigDecimal quantity, BigDecimal tokenPrice, User user) {
        Optional<SpotPosition> position = findPositionByToken(user, token);

        position.ifPresent(pos ->
                    pos.setQuantity(pos.getQuantity().add(quantity))
        );
        Instant lastBuyOrder = marketOrderRepository.getNewestOrderTimestamp(user.getId(), token);
        return position.orElseGet(() ->
                spotPositionRepository.save(new SpotPosition(token, quantity, tokenPrice, user, lastBuyOrder))
        );
    }
    private void validateResources(BigDecimal owned, BigDecimal order){
        if(owned.compareTo(order) < 0)
            throw new NotEnoughResourcesException(order, owned);
    }
}
