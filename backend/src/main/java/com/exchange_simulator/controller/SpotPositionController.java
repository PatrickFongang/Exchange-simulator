package com.exchange_simulator.controller;

import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.security.CustomUserDetails;
import com.exchange_simulator.service.SpotPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-positions")
@RequiredArgsConstructor
public class SpotPositionController {
    private final SpotPositionService spotPositionService;

    @GetMapping()
    public ResponseEntity<List<SpotPositionResponseDto>> getPortfolio(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(spotPositionService.getPortfolio(user.getId()));
    }

    @GetMapping("/{token}")
    public ResponseEntity<SpotPositionResponseDto> getSpotPosition(@PathVariable String token, @AuthenticationPrincipal CustomUserDetails user){
        return spotPositionService.findPositionByTokenUnlocked(user.getId(), token)
                .map(spotPositionService::getDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
