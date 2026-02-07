package dev.upscairs.cratesAndDropevents.db.daos;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.cratesAndDropevents.db.Serializer;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;

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

public class DropeventDao extends Dao {

    public DropeventDao(CratesAndDropevents plugin, DatabaseManager db) {
        super(plugin, db);
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS dropevents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    folder TEXT NOT NULL DEFAULT '',
                    render_item TEXT NOT NULL,
                    drop_range INTEGER NOT NULL,
                    event_time INTEGER NOT NULL,
                    drop_count INTEGER NOT NULL,
                    countdown_time INTEGER NOT NULL,
                    broadcast INTEGER NOT NULL DEFAULT 0,
                    teleportable INTEGER NOT NULL DEFAULT 0,
                    startup_command TEXT,
                    min_players INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER,
                    updated_at INTEGER
                );
                """;
        executeSimpleStatement(sql);
    }

    public void saveDropeventAsync(Dropevent dropevent, Consumer<Integer> callback) {
        final boolean isInsert = dropevent.getId() == 0;
        final long now = System.currentTimeMillis();

        final String sql = isInsert
                ? "INSERT INTO dropevents (folder, render_item, drop_range, event_time, drop_count, countdown_time, broadcast, teleportable, startup_command, min_players, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE dropevents SET folder = ?, render_item = ?, drop_range = ?, event_time = ?, drop_count = ?, countdown_time = ?, broadcast = ?, teleportable = ?, startup_command = ?, min_players = ?, updated_at = ? WHERE id = ?";

        final Object[] params = isInsert
                ? new Object[] {
                dropevent.getFolder() == null ? "" : dropevent.getFolder(),
                Serializer.itemStackToJson(dropevent.getItem()),
                dropevent.getDropRange(),
                dropevent.getEventTimeSec(),
                dropevent.getDropCount(),
                dropevent.getCountdownSec(),
                dropevent.isBroadcasting() ? 1 : 0,
                dropevent.isTeleportable() ? 1 : 0,
                dropevent.getStartupCommand(),
                dropevent.getMinPlayers(),
                now,
                now
        }
                : new Object[] {
                dropevent.getFolder() == null ? "" : dropevent.getFolder(),
                Serializer.itemStackToJson(dropevent.getItem()),
                dropevent.getDropRange(),
                dropevent.getEventTimeSec(),
                dropevent.getDropCount(),
                dropevent.getCountdownSec(),
                dropevent.isBroadcasting() ? 1 : 0,
                dropevent.isTeleportable() ? 1 : 0,
                dropevent.getStartupCommand(),
                dropevent.getMinPlayers(),
                now,
                dropevent.getId()
        };

        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (isInsert) {
                executeUpdateReturningKey(sql, params)
                        .ifPresent(id -> getPlugin().getServer().getScheduler().runTask(
                                getPlugin(), () -> {
                                    dropevent.setId(id);
                                    if (callback != null) callback.accept(id);
                                }
                        ));
            } else {
                executeUpdate(sql, params);
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    if (callback != null) callback.accept(dropevent.getId());
                });
            }
        });
    }

    public Optional<Dropevent> getDropeventById(int id) {
        final String sql = "SELECT * FROM dropevents WHERE id = ?";
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDropevent(rs));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching dropevent by id " + id, e);
            return Optional.empty();
        }
    }

    public void getDropeventByIdAsync(int id, Consumer<Optional<Dropevent>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            Optional<Dropevent> result = getDropeventById(id);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(result));
        });
    }

    public List<Dropevent> getAllDropevents() {
        final String sql = "SELECT * FROM dropevents";
        List<Dropevent> out = new ArrayList<>();
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapDropevent(rs));
            return out;

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching all dropevents", e);
            return Collections.emptyList();
        }
    }

    public void getAllAsync(Consumer<List<Dropevent>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<Dropevent> events = getAllDropevents();
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(events));
        });
    }

    public void deleteDropeventByIdAsync(int id) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            final String sql = "DELETE FROM dropevents WHERE id = ?";
            executeUpdate(sql, id);
        });
    }

    private Dropevent mapDropevent(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String folder = rs.getString("folder");
        String renderItemJson = rs.getString("render_item");
        int dropRange = rs.getInt("drop_range");
        int eventTime = rs.getInt("event_time");
        int dropCount = rs.getInt("drop_count");
        int countdownTime = rs.getInt("countdown_time");
        boolean broadcast = rs.getInt("broadcast") != 0;
        boolean teleportable = rs.getInt("teleportable") != 0;
        String startupCommand = rs.getString("startup_command");
        int minPlayers = rs.getInt("min_players");
        long createdAt = rs.getLong("created_at");
        long updatedAt = rs.getLong("updated_at");

        return new Dropevent(
                id,
                folder,
                Serializer.jsonToItemStack(renderItemJson),
                dropRange,
                eventTime,
                dropCount,
                countdownTime,
                broadcast,
                teleportable,
                startupCommand,
                minPlayers
        );
    }
}
