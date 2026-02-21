package com.exchange_simulator.mapper;

import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.entity.SpotPosition;
import com.exchange_simulator.service.CryptoDataService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SpotPositionMapper {
    @Autowired
    protected CryptoDataService cryptoDataService;

    @Mapping(target = "positionId", source = "id")
    @Mapping(target = "positionValue", expression =
            "java(cryptoDataService.getPrice(entity.getToken())" +
                    ".multiply(entity.getQuantity()))")
    public abstract SpotPositionResponseDto toDto(SpotPosition entity);
}
