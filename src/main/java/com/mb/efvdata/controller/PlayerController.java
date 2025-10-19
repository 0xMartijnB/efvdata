package com.mb.efvdata.controller;

import com.mb.efvdata.model.Captain;
import com.mb.efvdata.model.Player;
import com.mb.efvdata.service.CaptainService;
import com.mb.efvdata.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // allow frontend (e.g. Vercel) to call this
public class PlayerController {

    private final PlayerService playerService;
    private final CaptainService captainService;

    public PlayerController(PlayerService playerService, CaptainService captainService) {
        this.playerService = playerService;
        this.captainService = captainService;
    }

    @RequestMapping("/api/players")
    public Map<String, Object> getPlayers(
            @RequestParam int fplId,
            @RequestParam int topManagersCount
    ) {
        List<Player> players = playerService.getPlayers(fplId, topManagersCount);

        Map<String, Object> response = new HashMap<>();
        response.put("players", players);

        return response;
    }

    @RequestMapping("/api/captains")
    public Map<String, Object> getCapaintedPlayers(
            @RequestParam int topManagersCount
    ) {
        List<Captain> captains = captainService.getCaptains(topManagersCount);

        Map<String, Object> response = new HashMap<>();
        response.put("captains", captains);
        return response;
    }
}
