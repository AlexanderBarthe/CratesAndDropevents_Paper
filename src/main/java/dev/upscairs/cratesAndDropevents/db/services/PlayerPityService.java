package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.db.daos.PlayerPityDao;
import dev.upscairs.utility.Tuple;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerPityService {

    private record PityKey(int rewardId, UUID playerUuid) {}

    private final PlayerPityDao dao;
    private final JavaPlugin plugin;

    private final Set<PityKey> pitiedPlayerCache = ConcurrentHashMap.newKeySet();

    private static final long CACHE_REFRESH_DELAY = 20L * 60 * 5;

    public PlayerPityService(PlayerPityDao dao, JavaPlugin plugin) {
        this.dao = dao;
        this.plugin = plugin;
        startAutoRefresh(CACHE_REFRESH_DELAY);
    }

    public boolean isPitied(int rewardId, OfflinePlayer player) {
        if (player == null) return false;
        return pitiedPlayerCache.contains(new PityKey(rewardId, player.getUniqueId()));
    }

    public void addPitiedPlayer(int rewardId, OfflinePlayer player) {
        if (player == null) return;
        PityKey key = new PityKey(rewardId, player.getUniqueId());

        pitiedPlayerCache.add(key);
        dao.addPlayerPityAsync(rewardId, player.getUniqueId().toString());
    }

    public void removePlayerPity(int rewardId, OfflinePlayer player) {
        if (player == null) return;
        PityKey key = new PityKey(rewardId, player.getUniqueId());

        pitiedPlayerCache.remove(key);
        dao.removePlayerPityAsync(rewardId, player.getUniqueId().toString());
    }

    public void removePityOfReward(int rewardId) {
        pitiedPlayerCache.removeIf(key -> key.rewardId == rewardId);
        dao.removePityOfRewardAsync(rewardId);
    }

    private void startAutoRefresh(long intervalTicks) {

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshCacheAsync,
                40L, intervalTicks
        );
    }

    public void refreshCacheAsync() {
        Consumer<List<Tuple<Integer, String>>> consumer = tuples -> {
            Set<PityKey> newSet = ConcurrentHashMap.newKeySet();

            for (Tuple<Integer, String> t : tuples) {
                try {
                    UUID uuid = UUID.fromString(t.getSecond());
                    newSet.add(new PityKey(t.getFirst(), uuid));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in player_pity DB: " + t.getSecond());
                }
            }

            pitiedPlayerCache.clear();
            pitiedPlayerCache.addAll(newSet);

        };
        dao.getAllPityEntriesAsync(consumer);
    }
}
