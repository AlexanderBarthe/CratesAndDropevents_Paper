package dev.upscairs.cratesAndDropevents.db;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class RewardDao {

    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public RewardDao(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS rewards (
                    id INT PRIMARY KEY,
                    crate_id INT NOT NULL,
                    reward_type TEXT NOT NULL,
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


}
