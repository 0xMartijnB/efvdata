package com.mb.efvdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.efvdata.model.Player;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlayerService {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String base_url = "https://fantasy.espngoal.nl/api/";
    int gw = 8;

    public PlayerService(RestTemplate restTemplate, ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    public List<Player> getPlayers(int fplId, int topManagersCount) {
        List<Player> result = new ArrayList<>();

        try {
            // 1️⃣ Get team data from fantasy API
            String managerUrl = base_url + "entry/" + fplId + "/event/" + gw + "/picks/";
            String response = restTemplate.getForObject(managerUrl, String.class);

            // 2️⃣ Parse JSON response
            Map<?, ?> data = mapper.readValue(response, Map.class);
            List<Map<String, Object>> picks = (List<Map<String, Object>>) data.get("picks");

            // 3️⃣ Extract player IDs
            List<Integer> playerIds = new ArrayList<>();
            for (Map<String, Object> pick : picks) {
                playerIds.add((Integer) pick.get("element"));
            }

            // 4️⃣ Query Supabase
            String sql = """
                SELECT 
                    fpl_name,
                    team,
                    ownership
                FROM (
                SELECT 
                    fpl_id,
                    (COUNT(*)::float / ?) * 100 AS ownership
                FROM players_picked
                WHERE fpl_id = ANY(?)
                  AND event = ?
                  AND _rank <= ?
                GROUP BY fpl_id
                LIMIT 15
                ) players
                LEFT JOIN player_mapping
                ON players.fpl_id = player_mapping.fp_id
            """;

            try (Connection conn = DriverManager.getConnection(
                    System.getenv("SUPABASE_NEW_URL"),
                    System.getenv("SUPABASE_NEW_USER"),
                    System.getenv("SUPABASE_NEW_PASS")
            )) {
                PreparedStatement stmt = conn.prepareStatement(sql);

                Array idArray = conn.createArrayOf("INTEGER", playerIds.toArray());
                stmt.setInt(1, topManagersCount);  // divide by N (like 10000)
                stmt.setArray(2, idArray);
                stmt.setInt(3, gw);
                stmt.setInt(4, topManagersCount);  // limit _rank <= N

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("fpl_name");
                    String team = rs.getString("team");
                    double ownership = rs.getDouble("ownership");
                    result.add(new Player(name, team, ownership));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}