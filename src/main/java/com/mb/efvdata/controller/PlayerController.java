package com.mb.efvdata.controller;

import com.mb.efvdata.model.Player;
import com.mb.efvdata.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*") // allow frontend (e.g. Vercel) to call this
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public Map<String, Object> getPlayers(
            @RequestParam int fplId,
            @RequestParam int topManagersCount
    ) {
        List<Player> players = playerService.getPlayers(fplId, topManagersCount);

        Map<String, Object> response = new HashMap<>();
        response.put("players", players);

        return response;
    }
}
