package com.mb.efvdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.efvdata.model.Captain;
import com.mb.efvdata.model.Player;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CaptainService {
//    private final RestTemplate restTemplate;
//    private final ObjectMapper mapper;
    public int gw = 8;

    public CaptainService(RestTemplate restTemplate, ObjectMapper mapper) {
//        this.restTemplate = restTemplate;
//        this.mapper = mapper;
    }

    public List<Captain> getCaptains(int topManagersCount) {
        List<Captain> result = new ArrayList<>();
        // Query Supabase
        String sql = """
            SELECT
            	fpl_name,
            	captained
            FROM (
            SELECT
            	fpl_id,
            	(COUNT(*)::float / ?) * 100 AS captained
            FROM players_picked
            WHERE _rank <= ?
                AND "event" = ?
                AND is_captain IS TRUE
            GROUP BY 1
            ORDER BY 2 DESC
            LIMIT 10
            ) p
            LEFT JOIN player_mapping
            ON p.fpl_id = player_mapping.fpl_id
            ORDER BY 2 DESC
            """;

        try (Connection conn = DriverManager.getConnection(
                System.getenv("SUPABASE_NEW_URL"), // Prod env
                System.getenv("SUPABASE_NEW_USER"),
                System.getenv("SUPABASE_NEW_PASS")
        )) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, topManagersCount);
            stmt.setInt(2, topManagersCount);
            stmt.setInt(3, gw);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("fpl_name");
                double captain_percent = rs.getDouble("captained");
                result.add(new Captain(name, captain_percent));

            }
        } catch (Exception e) {
            e.printStackTrace();
    }
        return result;
    }
}