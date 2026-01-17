package dev.upscairs.cratesAndDropevents.file_resources;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.cratesAndDropevents.helper.FolderizableElement;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CrateStorage {

    private static FileConfiguration config;
    private static File file;

    private static final String fileName = "crates.yml";


    public static void init(JavaPlugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), fileName);

        boolean generateSample = false;

        if (!file.exists()) {
            try {
                file.createNewFile();
                generateSample = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (generateSample) saveCrate(createExampleCrate());
    }


    public static void saveCrate(Crate crate) {

        config.set("crates." + crate.getName(), crate);
        saveFile();
    }

    public static void removeCrate(String id) {
        config.set("crates." + id, null);
        saveFile();
    }

    public static List<String> getCrateIds() {
        ConfigurationSection section = config.getConfigurationSection("crates");
        if (section == null) return new ArrayList<>();
        return new ArrayList<>(section.getKeys(false));
    }

    public static List<Crate> getAll() {
        List<Crate> list = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("crates");
        if (section == null) return list;

        for (String key : section.getKeys(false)) {
            Object obj = config.get("crates." + key);

            if (obj instanceof Crate crate) {

                //Fix if render item is corrupted
                if(crate.getRenderItem() == null) crate.setRenderItem(new ItemStack(Material.PLAYER_HEAD));

                list.add(crate);
            }
        }
        return list;
    }

    public static Crate getCrateById(String id) {
        Object obj = config.get("crates." + id);
        if (obj instanceof Crate) {
            return (Crate) obj;
        }
        return null;
    }

    public static List<Crate> getCratesInFolder(String folderPath) {
        return getAll().stream().filter(crate -> crate.getFolder().equals(folderPath)).toList();
    }

    public static Set<String> getAllFolderPaths() {
        return getAll().stream().map(FolderizableElement::getFolder).collect(Collectors.toSet());
    }

    public static Set<String> getSubfolders(String folderPath) {

        Set<String> subfolders = new HashSet<>();

        int subfolderDepth = folderPath.split("/").length;

        for(String currentFolderPath :  getAllFolderPaths()) {
            if(!currentFolderPath.startsWith(folderPath)) continue;

            String[] path =  currentFolderPath.split("/");

            if(path.length > subfolderDepth) {
                String subfolderName = path[subfolderDepth];
                subfolders.add(folderPath + "/" + subfolderName);
            }
        }

        return subfolders;
    }


    private static void saveFile() {
        try {
            config.save(file);
        } catch (IOException ignored) {}
    }

    private static Crate createExampleCrate() {
        Crate crate = new Crate("SampleCrate", "");
        crate.setPittySystem(true);

        CrateReward dirtReward = new CrateReward(
                List.of(
                        new SoundRewardEvent("minecraft:entity.cat.ambient", 1, 0.5f),
                        new ItemRewardEvent(new ItemStack(Material.DIRT))),
                Set.of(),
                CratesAndDropevents.getInstance());

        CrateReward diamondReward = new CrateReward(
                List.of(
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new ItemRewardEvent(new ItemStack(Material.DIAMOND))),
                Set.of(),
                CratesAndDropevents.getInstance());

        CrateReward netheriteReward = new CrateReward(
                List.of(
                        new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1),
                        new ItemRewardEvent(new ItemStack(Material.NETHERITE_INGOT))),
                Set.of(),
                CratesAndDropevents.getInstance());

        CrateReward beaconReward = new CrateReward(
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
                        new CommandRewardEvent("say %p just pulled a the legendary reward: Beacon", CratesAndDropevents.getInstance()),
                        new ItemRewardEvent(new ItemStack(Material.BEACON))),
                Set.of(),
                CratesAndDropevents.getInstance());

        crate.addReward(dirtReward, 700);
        crate.addReward(diamondReward, 100);
        crate.addReward(netheriteReward, 100);
        crate.addReward(beaconReward, 100);

        return crate;
    }
}
