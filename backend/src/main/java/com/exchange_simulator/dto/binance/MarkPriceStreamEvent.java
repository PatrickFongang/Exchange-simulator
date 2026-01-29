package com.exchange_simulator.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a user returned by the API.
 * <a href="https://developers.binance.com/docs/derivatives/usds-margined-futures/websocket-market-streams/Mark-Price-Stream">...</a>
 */
public class MarkPriceStreamEvent {
        @Getter
        @JsonProperty("e")
        String eventType;

        @Getter
        @JsonProperty("E")
        Instant eventsTime;

        @Getter
        @Setter
        @JsonProperty("s")
        String symbol;

        @Getter
        @JsonProperty("p")
        BigDecimal markPrice;

        @Getter
        @JsonProperty("i")
        BigDecimal indexPrice;

        @Getter
        @JsonProperty("P")
        BigDecimal estimatedSettlePrice;

        @Getter
        @JsonProperty("r")
        BigDecimal fundingRate;

        @Getter
        @JsonProperty("T")
        Instant nextFundingTime;
}