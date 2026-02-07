package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.cratesAndDropevents.db.daos.*;

public class DbServices {

    private final CrateService crateService;
    private final CrateRewardService crateRewardService;
    private final PlayerPityService playerPityService;
    private final DropeventService dropeventService;
    private final DropService dropService;

    public DbServices(CratesAndDropevents plugin, DatabaseManager db) {
        this.playerPityService = new PlayerPityService(new PlayerPityDao(plugin, db), plugin);
        this.crateRewardService = new CrateRewardService(
                playerPityService, new RewardDao(plugin, db), plugin);
        this.crateService = new CrateService(
                crateRewardService, new CrateDao(plugin, db), plugin);
        this.dropService = new DropService(new DropDao(plugin, db), plugin);
        this.dropeventService = new DropeventService(dropService, new DropeventDao(plugin, db), plugin);
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

    public DropeventService getDropeventService() {
        return dropeventService;
    }

    public DropService getDropService() {
        return dropService;
    }
}
