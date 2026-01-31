package dev.upscairs.cratesAndDropevents.db;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class CrateDao {
    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public CrateDao(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS crates (
              id INT PRIMARY KEY,
              name TEXT NOT NULL,
              crate_item TEXT,
              pity_system BOOLEAN NOT NULL DEFAULT FALSE,
              folder TEXT NOT NULL DEFAULT '',
              created_at DATETIME,
              updated_at DATETIME
            );
            """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create crates table", e);
        }
    }

    public CompletableFuture<Void> saveCrateAsync(Crate crate) {

        return CompletableFuture.runAsync(() -> {

            long id = crate.getId();
            String name = crate.getName();
            String crateItemString = Serializer.itemStackToJson(crate.getCrateItem());
            boolean pitySystem = crate.pittySystemActive();
            String folder = crate.getFolder();
            Map<CrateReward, Integer> rewards = crate.getRewards();

            if(id == 0) {

                long now = System.currentTimeMillis();

                String sql = """
                        INSERT INTO crates (name, crate_item, pity_system, folder, created_at, updated_at)
                        VALUES (?,?,?,?,?,?)
                        """;
                try (Connection c = db.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name);
                    ps.setString(2, crateItemString);
                    ps.setBoolean(3, pitySystem);
                    ps.setString(4, folder);
                    ps.setLong(5, now);
                    ps.setLong(6, now);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            long generatedId = keys.getLong(1);
                            crate.setId(generatedId);
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Fehler beim INSERT der Crate: " + name, e);
                }





            }
        });
    }

    public CompletableFuture<Optional<String>> loadCrateAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT data FROM crates WHERE name = ?";
            try (Connection c = db.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString("data"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Crate: " + name, e);
            }
            return Optional.empty();
        });
    }

    // Optional: loadAll
    public CompletableFuture<Map<String, String>> loadAllAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> out = new HashMap<>();
            String sql = "SELECT name, data FROM crates";
            try (Connection c = db.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("name"), rs.getString("data"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden aller Crates", e);
            }
            return out;
        });
    }
}
