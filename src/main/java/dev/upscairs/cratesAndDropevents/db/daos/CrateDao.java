package dev.upscairs.cratesAndDropevents.db.daos;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.cratesAndDropevents.db.Serializer;
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

public class CrateDao extends Dao {

    public CrateDao(JavaPlugin plugin, DatabaseManager db) {
        super(plugin, db);
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        final String sql = """
            CREATE TABLE IF NOT EXISTS crates (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              crate_item TEXT,
              pity_system INTEGER NOT NULL DEFAULT 0,
              folder TEXT NOT NULL DEFAULT '',
              created_at INTEGER,
              updated_at INTEGER
            );
            """;
        executeSimpleStatement(sql);
    }

    public void saveCrateAsync(Crate crate, Consumer<Integer> callback) {

        final boolean isInsert = crate.getId() == 0;
        final long now = System.currentTimeMillis();

        final String sql = isInsert
                ? """
              INSERT INTO crates (crate_item, pity_system, folder, created_at, updated_at)
              VALUES (?, ?, ?, ?, ?)
              """
                : """
              UPDATE crates
              SET crate_item = ?, pity_system = ?, folder = ?, updated_at = ?
              WHERE id = ?
              """;

        final Object[] params = isInsert
                ? new Object[] {
                Serializer.itemStackToJson(crate.getCrateItem()),
                crate.pitySystemActive(),
                crate.getFolder(),
                now,
                now
        }
                : new Object[] {
                Serializer.itemStackToJson(crate.getCrateItem()),
                crate.pitySystemActive(),
                crate.getFolder(),
                now,
                crate.getId()
        };

        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (isInsert) {
                executeUpdateReturningKey(sql, params)
                        .ifPresent(id ->
                                getPlugin().getServer().getScheduler().runTask(
                                        getPlugin(), () -> {
                                            crate.setId(id);
                                            if (callback != null) callback.accept(id);
                                        }
                                )
                        );
            }
            else {
                executeUpdate(sql, params);
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    if(callback != null) callback.accept(crate.getId());
                });
            }
        });
    }

    public Optional<Crate> getCrateById(int id) {

        final String sql = "SELECT * FROM crates WHERE id = ?";

        try (Connection c = getDb().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCrate(rs));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching crate by id " + id, e);
            return Optional.empty();
        }

    }

    public void getCrateByIdAsync(int id, Consumer<Optional<Crate>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            Optional<Crate> result = getCrateById(id);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(result));
        });
    }

    public List<Crate> getAllCrates() {

        final String sql = "SELECT * FROM crates";

        List<Crate> out = new ArrayList<>();
        try(Connection c = getDb().getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) out.add(mapCrate(rs));
            return out;

        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while fetching all crates", e);
            return Collections.emptyList();
        }

    }

    public void getAllAsync(Consumer<List<Crate>> callback) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<Crate> crates = getAllCrates();
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> callback.accept(crates));
        });
    }

    public void deleteCrateByIdAsync(int id) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            final String sql = "DELETE FROM crates WHERE id = ?";
            executeUpdate(sql, id);
        });

    }

    private Crate mapCrate(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        ItemStack crateItem = Serializer.jsonToItemStack(rs.getString("crate_item"));
        boolean pitySystem = rs.getBoolean("pity_system");
        String folder = rs.getString("folder");

        return new Crate(id, folder, crateItem, pitySystem);
    }
}
