package com.mb.efvdata.controller;

import com.mb.efvdata.model.Player;
import com.mb.efvdata.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*") // allow frontend (e.g. Vercel) to call this
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<Player> getPlayers(
            @RequestParam int fplId,
            @RequestParam int topManagersCount
    ) {
        return playerService.getPlayers(fplId, topManagersCount);
    }
}
