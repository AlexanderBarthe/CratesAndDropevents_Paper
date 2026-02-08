package dev.upscairs.cratesAndDropevents.db.daos;

import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

public class RewardDao extends Dao {

    public RewardDao(JavaPlugin plugin, DatabaseManager db) {
        super(plugin, db);
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS rewards (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    crate_id INTEGER NOT NULL REFERENCES crates(id) ON DELETE CASCADE,
                    probability INTEGER NOT NULL,
                    reward_sequence TEXT NOT NULL,
                    created_at INTEGER,
                    updated_at INTEGER
                );
                """;
        executeSimpleStatement(sql);
    }

    public void saveRewardAsync(CrateReward reward, Consumer<Integer> callback) {

        final boolean isInsert = reward.getId() == 0;
        final long now = System.currentTimeMillis();

        final String sql = isInsert
                ? "INSERT INTO rewards (crate_id, probability, reward_sequence, created_at, updated_at) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE rewards SET crate_id = ?, probability = ?, reward_sequence = ?, updated_at = ? WHERE id = ?";

        final Object[] params = isInsert
                ? new Object[] {
                reward.getCrateId(),
                reward.getProbability(),
                reward.sequenceToString(),
                now,
                now
        }
                : new Object[] {
                reward.getCrateId(),
                reward.getProbability(),
                reward.sequenceToString(),
                now,
                reward.getId()
        };

        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (isInsert) {
                executeUpdateReturningKey(sql, params)
                        .ifPresent(id -> getPlugin().getServer().getScheduler().runTask(
                                getPlugin(), () -> {
                                    reward.setId(id);
                                    if (callback != null) callback.accept(id);
                                }
                        ));
            }
            else {
                executeUpdate(sql, params);
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    if (callback != null) callback.accept(reward.getId());
                });
            }
        });
    }

    public Optional<CrateReward> getRewardById(int id) {
        String sql = "SELECT * FROM rewards WHERE id = ?";
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapReward(rs));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching reward by id " + id, e);
            return Optional.empty();
        }
    }

    public void getRewardByIdAsync(int id, Consumer<Optional<CrateReward>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            Optional<CrateReward> result = getRewardById(id);

            getPlugin().getServer().getScheduler().runTask(getPlugin(), () ->
                    callback.accept(result)
            );
        });
    }

    public List<CrateReward> getRewardsForCrate(int crateId) {
        final String sql = "SELECT * FROM rewards WHERE crate_id = ?";
        List<CrateReward> out = new ArrayList<>();
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, crateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapReward(rs));
            }
            return out;

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching rewards for crate " + crateId, e);
            return Collections.emptyList();
        }
    }

    public void getRewardsForCrateAsync(int crateId, Consumer<List<CrateReward>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<CrateReward> rewards = getRewardsForCrate(crateId);

            getPlugin().getServer().getScheduler().runTask(getPlugin(), () ->
                    callback.accept(rewards)
            );
        });
    }

    public List<CrateReward> getAllRewards() {
        final String sql = "SELECT * FROM rewards";

        List<CrateReward> out = new ArrayList<>();
        try(Connection c = getDb().getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapReward(rs));
            return out;

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching all rewards ", e);
            return Collections.emptyList();
        }

    }

    public void getAllAsync(Consumer<List<CrateReward>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<CrateReward> rewards = getAllRewards();
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(rewards));
        });
    }

    public void deleteRewardByIdAsync(int id) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            final String sql = "DELETE FROM rewards WHERE id = ?";
            executeUpdate(sql, id);
        });
    }


    private CrateReward mapReward(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int crateId = rs.getInt("crate_id");
        int probability = rs.getInt("probability");
        String rewardSequence = rs.getString("reward_sequence");
        return new CrateReward(id, crateId, probability, rewardSequence, getPlugin());
    }

}
