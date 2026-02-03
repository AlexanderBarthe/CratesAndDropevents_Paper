package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.daos.CrateDao;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CrateService {

    private final CrateRewardService rewardService;
    private final CrateDao dao;
    private final JavaPlugin plugin;

    private final Map<Integer, Crate> crateCache = new ConcurrentHashMap<>();

    private static final long CACHE_REFRESH_DELAY = 20L * 60 * 5;

    public CrateService(CrateRewardService rewardService, CrateDao rewardDao, JavaPlugin plugin) {
        this.rewardService = rewardService;
        this.dao = rewardDao;
        this.plugin = plugin;
        startAutoRefresh(CACHE_REFRESH_DELAY);
    }

    public Crate getCrateById(int id) {
        return crateCache.get(id);
    }

    public List<Crate> getAllCrates() {
        return crateCache.values().stream().toList();
    }

    public void createCrate(Crate crate) {
        crate.setId(0);

        Consumer<Integer> callback = id -> {
            crate.setId(id);
            crateCache.put(id, crate);
        };

        dao.saveCrateAsync(crate, callback);
    }

    public void updateCrate(Crate crate) {
        dao.saveCrateAsync(crate, null);
        crateCache.put(crate.getId(), crate);
    }

    public void deleteCrateById(int id) {
        dao.deleteCrateByIdAsync(id);
        crateCache.remove(id);
        rewardService.getRewardsForCrate(id);
    }


    private void startAutoRefresh(long intervalTicks) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshCacheAsync,
                0, intervalTicks);
    }

    public void refreshCacheAsync() {
        Consumer<List<Crate>> consumer = crates -> {
            crateCache.clear();
            crates.forEach(crate -> crateCache.put(crate.getId(), crate));
        };
        dao.getAllCratesAsync(consumer);
    }

}
