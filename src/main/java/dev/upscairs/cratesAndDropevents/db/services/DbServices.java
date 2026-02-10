package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.cratesAndDropevents.db.DatabaseManager;
import dev.upscairs.cratesAndDropevents.db.daos.*;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public class DbServices {

    private final CrateService crateService;
    private final CrateRewardService crateRewardService;
    private final PlayerPityService playerPityService;
    private final DropeventService dropeventService;
    private final DropService dropService;

    private final CratesAndDropevents plugin;

    public DbServices(CratesAndDropevents plugin, DatabaseManager db) {
        this.playerPityService = new PlayerPityService(new PlayerPityDao(plugin, db), plugin);
        this.crateRewardService = new CrateRewardService(
                playerPityService, new RewardDao(plugin, db), plugin);
        this.crateService = new CrateService(
                crateRewardService, new CrateDao(plugin, db), plugin);
        this.dropService = new DropService(new DropDao(plugin, db), plugin);
        this.dropeventService = new DropeventService(dropService, new DropeventDao(plugin, db), plugin);

        this.plugin = plugin;

        if(db.isFreshDatabase()) generateSampleData();

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

    private void generateSampleData() {
        Crate crate = new Crate("Sample Crate" , "");
        crate.setPitySystemActive(true);

        crateService.createCrate(crate, this::generateSampleDropevent);

        CrateReward dirtReward = new CrateReward(0, 1, 700,
                List.of(
                        new SoundRewardEvent("minecraft:entity.cat.ambient", 1, 0.5f),
                        new ItemRewardEvent(new ItemStack(Material.DIRT))),
                plugin);

        CrateReward diamondReward = new CrateReward(0, 1, 100,
                List.of(
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new ItemRewardEvent(new ItemStack(Material.DIAMOND))),
                plugin);

        CrateReward netheriteReward = new CrateReward(0, 1, 100,
                List.of(
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new ItemRewardEvent(new ItemStack(Material.NETHERITE_INGOT))),
                plugin);

        CrateReward beaconReward = new CrateReward(0, 1, 100,
                List.of(
                        new SoundRewardEvent("minecraft:entity.ender_dragon.ambient", 1, 1),
                        new MessageRewardEvent("<rainbow>Legendary Reward</rainbow>"),
                        new DelayRewardEvent(80, CratesAndDropevents.getInstance()),
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new MessageRewardEvent("3"),
                        new DelayRewardEvent(20, CratesAndDropevents.getInstance()),
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new MessageRewardEvent("2"),
                        new DelayRewardEvent(20, CratesAndDropevents.getInstance()),
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new MessageRewardEvent("1"),
                        new DelayRewardEvent(20, CratesAndDropevents.getInstance()),
                        new SoundRewardEvent("minecraft:block.portal.travel", 1, 2),
                        new CommandRewardEvent("particle totem_of_undying %l 0 0 0 0.2 20 normal", CratesAndDropevents.getInstance()),
                        new CommandRewardEvent("say %p just pulled a legendary reward: Beacon", CratesAndDropevents.getInstance()),
                        new ItemRewardEvent(new ItemStack(Material.BEACON))),
                plugin);

        crateRewardService.createReward(dirtReward);
        crateRewardService.createReward(diamondReward);
        crateRewardService.createReward(netheriteReward);
        crateRewardService.createReward(beaconReward);
    }

    private void generateSampleDropevent(Crate crate) {
        Dropevent dropevent = new Dropevent("Sample Dropevent", "");
        dropevent.setBroadcasting(true);
        dropevent.setTeleportable(true);
        dropevent.setCountdownSec(10);
        dropevent.setDropCount(100);
        dropevent.setEventTimeSec(20);
        dropevent.setDropRange(25);
        dropevent.setMinPlayers(0);

        dropeventService.create(dropevent);

        Drop drop = new Drop(0, 1, 1000, crate.getCrateItem());

        dropService.createDrop(drop);

    }
}
