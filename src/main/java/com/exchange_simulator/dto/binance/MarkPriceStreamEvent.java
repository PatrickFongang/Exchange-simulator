package com.exchange_simulator.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a user returned by the API.
 * <a href="https://developers.binance.com/docs/derivatives/usds-margined-futures/websocket-market-streams/Mark-Price-Stream">...</a>
 */
public record MarkPriceStreamEvent(
        @JsonProperty("e")
        String eventType,

        @JsonProperty("E")
        Instant eventsTime,

        @JsonProperty("s")
        String symbol,

        @JsonProperty("p")
        BigDecimal markPrice,

        @JsonProperty("i")
        BigDecimal indexPrice,

        @JsonProperty("P")
        BigDecimal estimatedSettlePrice,

        @JsonProperty("r")
        BigDecimal fundingRate,

        @JsonProperty("T")
        Instant nextFundingTime
) {}