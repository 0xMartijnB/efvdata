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
    private final String base_url = "https://fantasy.espngoal.nl/api/";
    int gw = 8;

    public List<Player> getPlayers(int fplId, int topManagersCount) {
        List<Player> result = new ArrayList<>();

        try {
            // 1️⃣ Get team data from fantasy API
            RestTemplate restTemplate = new RestTemplate();
            String managerUrl = base_url + "entry/" + fplId + "/event/" + gw + "/picks/";
            String response = restTemplate.getForObject(managerUrl, String.class);

            // 2️⃣ Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
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
                    fpl_id,
                    COUNT(*)::float / ? AS ownership
                FROM players
                WHERE fpl_id = ANY(?)
                  AND event = ?
                  AND _rank <= ?
                GROUP BY fpl_id
                ORDER BY ownership DESC
                LIMIT 15
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
                    int id = rs.getInt("fpl_id");
                    double ownership = rs.getDouble("ownership");
                    result.add(new Player(String.valueOf(id), ownership));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}