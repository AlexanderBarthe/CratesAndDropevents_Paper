package dev.upscairs.cratesAndDropevents.db.daos;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.utility.Tuple;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PlayerPityDao extends Dao {

    public PlayerPityDao(CratesAndDropevents plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_pity (
                reward_id INTEGER NOT NULL REFERENCES rewards(id) ON DELETE CASCADE,
                player_uuid TEXT NOT NULL,
                PRIMARY KEY (reward_id, player_uuid)
            );
            """;
        executeSimpleStatement(sql);
    }

    public void addPlayerPityAsync(int rewardId, String playerUuid) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            String sql = "INSERT OR IGNORE INTO player_pity (reward_id, player_uuid) VALUES (?, ?)";
            executeUpdate(sql, rewardId, playerUuid);
        });
    }

    public void removePlayerPityAsync(int rewardId, String playerUuid) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            String sql = "DELETE FROM player_pity WHERE reward_id = ? AND player_uuid = ?";
            executeUpdate(sql, rewardId, playerUuid);
        });
    }

    public void removePityOfRewardAsync(int rewardId) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            String sql = "DELETE FROM player_pity WHERE reward_id = ?";
            executeUpdate(sql, rewardId);
        });
    }


    public void containsPlayerPityAsync(int rewardId, String playerUuid, Consumer<Boolean> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            boolean result = false;

            String sql = "SELECT 1 FROM player_pity WHERE reward_id = ? AND player_uuid = ? LIMIT 1";

            try (Connection c = getDb().getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, rewardId);
                ps.setString(2, playerUuid);

                try (ResultSet rs = ps.executeQuery()) {
                    result = rs.next();
                }

            } catch (SQLException e) {
                getPlugin().getLogger().log(
                        Level.SEVERE, "Error while checking player_pity (" + rewardId + ", " + playerUuid + ")", e);
            }

            boolean finalResult = result;
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () ->
                    callback.accept(finalResult)
            );
        });
    }

    public void getAllAsync(Consumer<List<Tuple<Integer, String>>> callback) {

        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            final String sql = "SELECT reward_id, player_uuid FROM player_pity";

            try (Connection c = getDb().getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Tuple<Integer, String>> out = new java.util.ArrayList<>();
                while (rs.next()) {
                    out.add(new Tuple<>(rs.getInt("reward_id"), rs.getString("player_uuid")));
                }

                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(out));


            } catch (SQLException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while fetching all player pity entries", e);
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(new ArrayList<>()));
            }

        });

    }


}
