package dev.upscairs.cratesAndDropevents.db.daos;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.cratesAndDropevents.helper.Serializer;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
import org.bukkit.inventory.ItemStack;
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

public class DropDao extends Dao {

    public DropDao(CratesAndDropevents plugin, DatabaseManager db) {
        super(plugin, db);
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS drops (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    dropevent_id INTEGER NOT NULL REFERENCES dropevents(id) ON DELETE CASCADE,
                    probability INTEGER NOT NULL,
                    item TEXT NOT NULL,
                    created_at INTEGER,
                    updated_at INTEGER
                );
                """;
        executeSimpleStatement(sql);
    }

    public void saveDropAsync(Drop drop, Consumer<Integer> callback) {
        final boolean isInsert = drop.getId() == 0;
        final long now = System.currentTimeMillis();

        final String sql = isInsert
                ? "INSERT INTO drops (dropevent_id, probability, item, created_at, updated_at) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE drops SET dropevent_id = ?, probability = ?, item = ?, updated_at = ? WHERE id = ?";

        final Object[] params = isInsert
                ? new Object[] {
                drop.getDropeventId(),
                drop.getProbability(),
                Serializer.itemStackToJson(drop.getItem()),
                now,
                now
        }
                : new Object[] {
                drop.getDropeventId(),
                drop.getProbability(),
                Serializer.itemStackToJson(drop.getItem()),
                now,
                drop.getId()
        };

        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (isInsert) {
                executeUpdateReturningKey(sql, params)
                        .ifPresent(id -> getPlugin().getServer().getScheduler().runTask(
                                getPlugin(), () -> {
                                    drop.setId(id);
                                    if (callback != null) callback.accept(id);
                                }
                        ));
            } else {
                executeUpdate(sql, params);
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    if (callback != null) callback.accept(drop.getId());
                });
            }
        });
    }

    public Optional<Drop> getDropById(int id) {
        String sql = "SELECT * FROM drops WHERE id = ?";
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDrop(rs));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching drop by id " + id, e);
            return Optional.empty();
        }
    }

    public void getDropByIdAsync(int id, Consumer<Optional<Drop>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            Optional<Drop> result = getDropById(id);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(result));
        });
    }

    public List<Drop> getDropsForDropevent(int dropeventId) {
        final String sql = "SELECT * FROM drops WHERE dropevent_id = ?";
        List<Drop> out = new ArrayList<>();
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, dropeventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapDrop(rs));
            }
            return out;

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching drops for dropevent " + dropeventId, e);
            return Collections.emptyList();
        }
    }

    public void getDropsForDropeventAsync(int dropeventId, Consumer<List<Drop>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<Drop> drops = getDropsForDropevent(dropeventId);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(drops));
        });
    }

    public List<Drop> getAllDrops() {
        final String sql = "SELECT * FROM drops";

        List<Drop> out = new ArrayList<>();
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapDrop(rs));
            return out;

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching all drops ", e);
            return Collections.emptyList();
        }
    }

    public void getAllAsync(Consumer<List<Drop>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<Drop> drops = getAllDrops();
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(drops));
        });
    }

    public void deleteDropByIdAsync(int id) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            final String sql = "DELETE FROM drops WHERE id = ?";
            executeUpdate(sql, id);
        });
    }

    private Drop mapDrop(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int dropeventId = rs.getInt("dropevent_id");
        int probability = rs.getInt("probability");
        ItemStack item = Serializer.jsonToItemStack(rs.getString("item"));
        return new Drop(id, dropeventId, probability, item);
    }
}
