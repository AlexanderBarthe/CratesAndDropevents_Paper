package dev.upscairs.cratesAndDropevents.db.daos;

import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;

public class Dao {

    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public Dao(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void executeSimpleStatement(String sql) {
        try(Connection c = getDb().getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Could not execute query " + sql , e);
        }
    }

    public int executeUpdate(String sql, Object... params) {
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            return ps.executeUpdate(); // affected rows

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Could not execute update: " + sql, e);
            return -1;
        }
    }

    protected Optional<Integer> executeUpdateReturningKey(
            String sql,
            Object... params
    ) {
        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return Optional.of(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Could not execute update: " + sql, e);
        }
        return Optional.empty();
    }


    public DatabaseManager getDb() {
        return db;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }


}
