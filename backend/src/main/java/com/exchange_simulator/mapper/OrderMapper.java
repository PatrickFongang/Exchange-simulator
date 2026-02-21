package com.exchange_simulator.mapper;

import com.exchange_simulator.dto.order.OrderResponseDto;
import com.exchange_simulator.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "entry", source = "tokenPrice")
    OrderResponseDto toDto(Order entity);
}
