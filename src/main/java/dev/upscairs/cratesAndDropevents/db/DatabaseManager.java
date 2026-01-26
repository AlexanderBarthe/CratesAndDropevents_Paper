package dev.upscairs.cratesAndDropevents.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private final HikariDataSource ds;

    public DatabaseManager(JavaPlugin plugin) {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        plugin.getDataFolder().mkdirs();
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setMaximumPoolSize(4);
        cfg.setPoolName(plugin.getName() + "-HikariPool");
        cfg.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON; PRAGMA synchronous=NORMAL;");

        this.ds = new HikariDataSource(cfg);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }
}