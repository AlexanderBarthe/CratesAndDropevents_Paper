package dev.upscairs.cratesAndDropevents.file_resources;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.cratesAndDropevents.db.services.CrateRewardService;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.db.services.DbServices;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        //if (generateSample) saveCrate(createExampleCrate());
    }

    public static void migrateFromYaml(DbServices dbServices,
                                       JavaPlugin plugin) {

        CrateService crateService = dbServices.getCrateService();
        CrateRewardService crateRewardService = dbServices.getCrateRewardService();


        if (config == null) {
            plugin.getLogger().warning("Crate migration aborted: config is null.");
            return;
        }

        if (!config.isConfigurationSection("crates")) {
            plugin.getLogger().info("No 'crates' section found — nothing to migrate.");
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String key : config.getConfigurationSection("crates").getKeys(false)) {
                try {
                    Object raw = config.get("crates." + key);
                    if (!(raw instanceof Map)) {
                        plugin.getLogger().warning("Skipped crate '" + key + "': unexpected format.");
                        continue;
                    }

                    Map<String, Object> crateMap = (Map<String, Object>) raw;

                    String folder = crateMap.getOrDefault("folder", "") instanceof String ? (String) crateMap.getOrDefault("folder", "") : "";

                    ItemStack crateItem = readItemFromObject(crateMap.get("crateItem"));
                    if (crateItem == null) {
                        plugin.getLogger().info("Crate '" + key + "' has no crateItem, using PLAYER_HEAD fallback.");
                        crateItem = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
                    }

                    boolean pitySystem = false;
                    if (crateMap.containsKey("pittySystem")) {
                        Object p = crateMap.get("pittySystem");
                        if (p instanceof Boolean) pitySystem = (Boolean) p;
                        else if (p instanceof String) pitySystem = Boolean.parseBoolean((String) p);
                    } else if (crateMap.containsKey("pitySystem")) {
                        Object p = crateMap.get("pitySystem");
                        if (p instanceof Boolean) pitySystem = (Boolean) p;
                        else if (p instanceof String) pitySystem = Boolean.parseBoolean((String) p);
                    }

                    Crate crate = new Crate(0, folder, crateItem, pitySystem);

                    crateService.createCrate(crate, createdCrate -> {
                        if (createdCrate == null) {
                            plugin.getLogger().warning("Crate create callback returned null for key: " + key);
                            return;
                        }

                        Object rewardsObj = crateMap.get("rewards");
                        if (!(rewardsObj instanceof List<?>)) return;

                        List<?> rewardsList = (List<?>) rewardsObj;
                        for (Object rewardEntry : rewardsList) {
                            try {
                                if (!(rewardEntry instanceof Map<?, ?>)) continue;
                                Map<String, Object> rewardMapWrapper = (Map<String, Object>) rewardEntry;

                                Map<String, Object> rewardInner = null;
                                Object inner = rewardMapWrapper.get("reward");
                                if (inner instanceof Map<?, ?>) rewardInner = (Map<String, Object>) inner;
                                else if (rewardMapWrapper.containsKey("reward")) {
                                    continue;
                                } else {
                                    rewardInner = rewardMapWrapper;
                                }

                                int chance = 0;
                                Object c = rewardMapWrapper.getOrDefault("chance", rewardMapWrapper.get("probability"));
                                if (c instanceof Number) chance = ((Number) c).intValue();
                                else if (c instanceof String) {
                                    try { chance = Integer.parseInt((String) c); } catch (NumberFormatException ignored) { chance = 0; }
                                } else {
                                    Object cc = rewardInner.get("chance");
                                    if (cc instanceof Number) chance = ((Number) cc).intValue();
                                }

                                List<CrateRewardEvent> sequence = new ArrayList<>();
                                Object eventsObj = rewardInner.get("events");
                                if (eventsObj instanceof List<?>) {
                                    List<?> eventsList = (List<?>) eventsObj;
                                    for (Object evRaw : eventsList) {
                                        try {
                                            if (!(evRaw instanceof Map<?, ?>)) continue;
                                            Map<String, Object> ev = (Map<String, Object>) evRaw;
                                            Object typeObj = ev.get("type");
                                            if (typeObj == null) continue;
                                            String type = typeObj.toString().toLowerCase();

                                            switch (type) {
                                                case "item": {
                                                    Object itemObj = ev.get("item");
                                                    ItemStack item = readItemFromObject(itemObj);
                                                    if (item != null) sequence.add(new ItemRewardEvent(item));
                                                    break;
                                                }
                                                case "command": {
                                                    Object cmd = ev.get("command");
                                                    if (cmd != null) sequence.add(new CommandRewardEvent(cmd.toString(), plugin));
                                                    break;
                                                }
                                                case "delay": {
                                                    Object ticksObj = ev.get("ticks");
                                                    int ticks = 0;
                                                    if (ticksObj instanceof Number) ticks = ((Number) ticksObj).intValue();
                                                    else if (ticksObj instanceof String) {
                                                        try { ticks = Integer.parseInt((String) ticksObj); } catch (NumberFormatException ignored) {}
                                                    }
                                                    sequence.add(new DelayRewardEvent(ticks, plugin));
                                                    break;
                                                }
                                                case "message": {
                                                    Object msg = ev.get("message");
                                                    if (msg != null) sequence.add(new MessageRewardEvent(msg.toString()));
                                                    break;
                                                }
                                                case "sound": {
                                                    Object sndObj = ev.getOrDefault("soundName", ev.get("sound"));
                                                    String soundName = sndObj != null ? sndObj.toString() : "";
                                                    float volume = 1.0f;
                                                    float pitch = 1.0f;
                                                    Object v = ev.get("volume");
                                                    Object p = ev.get("pitch");
                                                    if (v instanceof Number) volume = ((Number) v).floatValue();
                                                    else if (v instanceof String) {
                                                        try { volume = Float.parseFloat((String) v); } catch (NumberFormatException ignored) {}
                                                    }
                                                    if (p instanceof Number) pitch = ((Number) p).floatValue();
                                                    else if (p instanceof String) {
                                                        try { pitch = Float.parseFloat((String) p); } catch (NumberFormatException ignored) {}
                                                    }
                                                    sequence.add(new SoundRewardEvent(soundName, volume, pitch));
                                                    break;
                                                }
                                                default:
                                                    plugin.getLogger().warning("Unknown reward event type: " + type + " in crate " + key);
                                            }
                                        } catch (Throwable tEv) {
                                        }
                                    }
                                }

                                CrateReward crateReward = new CrateReward(0, createdCrate.getId(), chance, sequence, plugin);
                                try {
                                    crateRewardService.createReward(crateReward);
                                } catch (NoSuchMethodError | Exception e) {
                                    plugin.getLogger().warning("Could not call crateRewardService.createReward(...). Ensure service API is available.");
                                }

                            } catch (Throwable tReward) {
                            }
                        }
                    });

                } catch (Throwable t) {
                }
            }

            plugin.getLogger().info("Crates migration scheduled (DB inserts executed asynchronously by DAOs).");
        });
    }

    private static ItemStack readItemFromObject(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof ItemStack) return ((ItemStack) obj).clone();
            if (obj instanceof Map<?, ?>) {
                Object des = ConfigurationSerialization.deserializeObject((Map<String, Object>) obj);
                if (des instanceof ItemStack) return ((ItemStack) des).clone();
            }
        } catch (Exception e) {
        }
        return null;
    }


    /*
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
    }*/


    private static void saveFile() {
        try {
            config.save(file);
        } catch (IOException ignored) {}
    }

    /*
    private static Crate createExampleCrate() {
        Crate crate = new Crate("SampleCrate", "");
        crate.setPitySystemActive(true);

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
    }*/
}
