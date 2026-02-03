package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.cratesAndDropevents.db.daos.CrateDao;
import dev.upscairs.cratesAndDropevents.db.daos.PlayerPityDao;
import dev.upscairs.cratesAndDropevents.db.daos.RewardDao;
import org.bukkit.plugin.java.JavaPlugin;

public class DbServices {

    private final CrateService crateService;
    private final CrateRewardService crateRewardService;
    private final PlayerPityService playerPityService;

    public DbServices(JavaPlugin plugin, DatabaseManager db) {
        this.playerPityService = new PlayerPityService(new PlayerPityDao(plugin, db), plugin);
        this.crateRewardService = new CrateRewardService(
                playerPityService, new RewardDao(plugin, db), plugin);
        this.crateService = new CrateService(
                crateRewardService, new CrateDao(plugin, db), plugin);
    }

    public CrateService getCrateService() {
        return crateService;
    }

    public CrateRewardService getCrateRewardService() {
        return crateRewardService;
    }

    public PlayerPityService getPlayerPityService() {
        return playerPityService;
    }
}
