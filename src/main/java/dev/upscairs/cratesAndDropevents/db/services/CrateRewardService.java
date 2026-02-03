package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.db.daos.RewardDao;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CrateRewardService {

    private final PlayerPityService playerPityService;
    private final RewardDao dao;
    private final JavaPlugin plugin;

    private final Map<Integer, CrateReward> rewardCache = new ConcurrentHashMap<>();

    private static final long CACHE_REFRESH_DELAY = 20L * 60 * 5;

    public CrateRewardService(PlayerPityService playerPityService, RewardDao rewardDao, JavaPlugin plugin) {
        this.playerPityService = playerPityService;
        this.dao = rewardDao;
        this.plugin = plugin;
        startAutoRefresh(CACHE_REFRESH_DELAY);
    }

    public CrateReward getRewardById(int id) {
        return rewardCache.get(id);
    }

    public List<CrateReward> getRewardsForCrate(int crateId) {
        return rewardCache.values().stream().filter(reward -> reward.getCrateId() == crateId).toList();
    }

    public List<CrateReward> getAllRewards() {
        return rewardCache.values().stream().toList();
    }

    public void createReward(CrateReward reward) {
        reward.setId(0);

        Consumer<Integer> callback = id -> {
            reward.setId(id);
            rewardCache.put(id, reward);
        };
        dao.saveRewardAsync(reward, callback);
    }

    public void updateReward(CrateReward reward) {
        dao.saveRewardAsync(reward, null);
        rewardCache.put(reward.getId(), reward);
    }

    public void deleteRewardById(int id) {
        dao.deleteRewardByIdAsync(id);
        rewardCache.remove(id);
        playerPityService.removePityOfReward(id);
    }

    private void startAutoRefresh(long intervalTicks) {

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshCacheAsync,
                0L, intervalTicks
        );
    }

    public void refreshCacheAsync() {
        Consumer<List<CrateReward>> consumer = rewards -> {
            rewardCache.clear();
            rewards.forEach(reward -> rewardCache.put(reward.getId(), reward));
        };
        dao.getAllRewardsAsync(consumer);
    }


}
