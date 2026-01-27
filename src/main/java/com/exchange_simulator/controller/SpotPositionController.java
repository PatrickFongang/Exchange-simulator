package com.exchange_simulator.controller;

import com.exchange_simulator.dto.position.SpotPositionResponseDto;
import com.exchange_simulator.service.SpotPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-positions")
@RequiredArgsConstructor
public class SpotPositionController {
    private final SpotPositionService spotPositionService;

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<List<SpotPositionResponseDto>> getPortfolio(@PathVariable Long userId)
    {
        return ResponseEntity.ok(spotPositionService.getPortfolio(userId));
    }
}
