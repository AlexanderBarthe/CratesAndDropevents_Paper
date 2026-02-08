package dev.upscairs.cratesAndDropevents.file_resources;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.cratesAndDropevents.db.services.CrateRewardService;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.db.services.DropService;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public final class MigrationUtils {

    // ----------------------------
    // Crates Migration (Map-basiert)
    // ----------------------------
    @SuppressWarnings("unchecked")
    public static void migrateCratesFromMap(Map<String, Object> root,
                                            CrateService crateService,
                                            CrateRewardService crateRewardService,
                                            JavaPlugin plugin) {

        if (root == null || root.isEmpty()) {
            plugin.getLogger().info("Keine Crates-Map gefunden, Migration übersprungen.");
            return;
        }

        Object cratesObj = root.get("crates");
        if (!(cratesObj instanceof Map<?, ?>)) {
            plugin.getLogger().info("Kein 'crates' Eintrag in Map, überspringe Crate-Migration.");
            return;
        }

        Map<String, Object> cratesMap = (Map<String, Object>) cratesObj;

        // auf MAIN thread ausführen (ItemStack handling)
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String key : cratesMap.keySet()) {
                try {
                    Object raw = cratesMap.get(key);
                    if (!(raw instanceof Map)) {
                        plugin.getLogger().warning("Skipping crate '" + key + "' (unexpected format)");
                        continue;
                    }

                    Map<String, Object> crateMap = (Map<String, Object>) raw;

                    String folder = crateMap.getOrDefault("folder", "") instanceof String ? (String) crateMap.getOrDefault("folder", "") : "";

                    ItemStack crateItem = readItemFromObject(crateMap.get("crateItem"));
                    if (crateItem == null) crateItem = new ItemStack(org.bukkit.Material.PLAYER_HEAD);

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

                    // persist crate, in callback create rewards
                    crateService.createCrate(crate, createdCrate -> {
                        if (createdCrate == null) {
                            plugin.getLogger().warning("Created crate callback null for " + key);
                            return;
                        }

                        Object rewardsObj = crateMap.get("rewards");
                        if (!(rewardsObj instanceof List<?>)) return;

                        List<?> rewardsList = (List<?>) rewardsObj;
                        for (Object rewardEntry : rewardsList) {
                            try {
                                if (!(rewardEntry instanceof Map<?, ?>)) continue;
                                Map<String, Object> wrapper = (Map<String, Object>) rewardEntry;

                                Map<String, Object> rewardInner;
                                if (wrapper.containsKey("reward") && wrapper.get("reward") instanceof Map<?, ?>) {
                                    rewardInner = (Map<String, Object>) wrapper.get("reward");
                                } else {
                                    rewardInner = (Map<String, Object>) wrapper;
                                }

                                int chance = extractInt(wrapper, "chance", extractInt(rewardInner, "chance", 0));

                                // parse events
                                List<CrateRewardEvent> seq = new ArrayList<>();
                                Object eventsObj = rewardInner.get("events");
                                if (eventsObj instanceof List<?>) {
                                    List<?> events = (List<?>) eventsObj;
                                    for (Object evRaw : events) {
                                        if (!(evRaw instanceof Map<?, ?>)) continue;
                                        Map<String, Object> ev = (Map<String, Object>) evRaw;
                                        String type = ev.getOrDefault("type", "").toString().toLowerCase(Locale.ROOT);
                                        switch (type) {
                                            case "item": {
                                                ItemStack item = readItemFromObject(ev.get("item"));
                                                if (item != null) seq.add(new ItemRewardEvent(item));
                                                break;
                                            }
                                            case "command": {
                                                Object cmd = ev.get("command");
                                                if (cmd != null) seq.add(new CommandRewardEvent(cmd.toString(), plugin));
                                                break;
                                            }
                                            case "delay": {
                                                int ticks = extractInt(ev, "ticks", 0);
                                                seq.add(new DelayRewardEvent(ticks, plugin));
                                                break;
                                            }
                                            case "message": {
                                                Object m = ev.get("message");
                                                if (m != null) seq.add(new MessageRewardEvent(m.toString()));
                                                break;
                                            }
                                            case "sound": {
                                                String soundName = ev.getOrDefault("soundName", ev.get("sound")).toString();
                                                float volume = extractFloat(ev, "volume", 1.0f);
                                                float pitch = extractFloat(ev, "pitch", 1.0f);
                                                seq.add(new SoundRewardEvent(soundName, volume, pitch));
                                                break;
                                            }
                                            default:
                                                plugin.getLogger().warning("Unknown crate reward event type '" + type + "' in crate " + key);
                                        }
                                    }
                                }

                                CrateReward reward = new CrateReward(0, createdCrate.getId(), chance, seq, plugin);
                                crateRewardService.createReward(reward);

                            } catch (Throwable t) {
                                plugin.getLogger().log(Level.WARNING, "Failed to migrate reward for crate " + key, t);
                            }
                        }

                    }); // end crate create callback

                } catch (Throwable t) {
                    plugin.getLogger().log(Level.WARNING, "Failed to migrate crate " + key, t);
                }
            } // end for
            plugin.getLogger().info("Crate migration scheduled.");
        });
    }

    // ----------------------------
    // Dropevents Migration (Map-basiert)
    // ----------------------------
    @SuppressWarnings("unchecked")
    public static void migrateDropeventsFromMap(Map<String, Object> root,
                                                DropeventService dropeventService,
                                                DropService dropService,
                                                JavaPlugin plugin) {

        if (root == null || root.isEmpty()) {
            plugin.getLogger().info("No dropevents map found, skipping.");
            return;
        }

        Object eventsObj = root.get("events");
        if (!(eventsObj instanceof Map<?, ?>)) {
            plugin.getLogger().info("No 'events' section in map, skipping dropevent migration.");
            return;
        }

        Map<String, Object> eventsMap = (Map<String, Object>) eventsObj;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String key : eventsMap.keySet()) {
                try {
                    Object raw = eventsMap.get(key);
                    if (!(raw instanceof Map)) {
                        plugin.getLogger().warning("Skipping dropevent '" + key + "' unexpected format");
                        continue;
                    }
                    Map<String, Object> evMap = (Map<String, Object>) raw;

                    String folder = evMap.getOrDefault("folder", "") instanceof String ? (String) evMap.getOrDefault("folder", "") : "";
                    ItemStack renderItem = readItemFromObject(evMap.get("renderItem"));
                    if (renderItem == null) renderItem = new ItemStack(org.bukkit.Material.FIREWORK_ROCKET);

                    int dropRange = extractInt(evMap, "dropRange", 100);
                    int eventTimeSec = extractInt(evMap, "eventTimeSec", 120);
                    int dropCount = extractInt(evMap, "dropCount", 1);
                    int countdownSec = extractInt(evMap, "countdownSec", 60);
                    boolean broadcast = evMap.getOrDefault("broadcast", false) instanceof Boolean && (Boolean) evMap.getOrDefault("broadcast", false);
                    boolean teleportable = evMap.getOrDefault("teleportable", false) instanceof Boolean && (Boolean) evMap.getOrDefault("teleportable", false);
                    String startupCommand = evMap.getOrDefault("startupCommand", "") instanceof String ? (String) evMap.getOrDefault("startupCommand", "") : "";
                    int minPlayers = extractInt(evMap, "minPlayers", 0);

                    Dropevent dropevent = new Dropevent(0, folder, renderItem, dropRange, eventTimeSec, dropCount, countdownSec, broadcast, teleportable, startupCommand, minPlayers);

                    dropeventService.create(dropevent, createdEvent -> {
                        if (createdEvent == null) {
                            plugin.getLogger().warning("Created dropevent callback returned null for " + key);
                            return;
                        }

                        Object dropsObj = evMap.get("drops");
                        if (!(dropsObj instanceof List<?>)) return;

                        List<?> dropsList = (List<?>) dropsObj;
                        for (int i = 0; i < dropsList.size(); i++) {
                            try {
                                Object dropRaw = dropsList.get(i);
                                if (!(dropRaw instanceof Map)) continue;
                                Map<String, Object> dropMap = (Map<String, Object>) dropRaw;

                                ItemStack dropItem = readItemFromObject(dropMap.get("item"));
                                if (dropItem == null) {
                                    plugin.getLogger().warning("Skipping drop for event " + key + " index " + i + " due to missing item");
                                    continue;
                                }
                                int chance = extractInt(dropMap, "chance", extractInt(dropMap, "probability", 100));
                                Drop drop = new Drop(0, createdEvent.getId(), chance, dropItem);
                                dropService.createDrop(drop);
                            } catch (Throwable td) {
                                plugin.getLogger().log(Level.WARNING, "Failed to migrate drop for event " + key + " index " + i, td);
                            }
                        }

                    });

                } catch (Throwable t) {
                    plugin.getLogger().log(Level.WARNING, "Failed to migrate dropevent " + key, t);
                }
            }
            plugin.getLogger().info("Dropevent migration scheduled.");
        });
    }

    // ----------------------------
    // Helper: Item deserialisieren (ItemStack)
    // ----------------------------
    @SuppressWarnings("unchecked")
    public static ItemStack readItemFromObject(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof ItemStack) return ((ItemStack) obj).clone();
            if (obj instanceof Map<?, ?>) {
                Object des = ConfigurationSerialization.deserializeObject((Map<String, Object>) obj);
                if (des instanceof ItemStack) return ((ItemStack) des).clone();
            }
        } catch (Throwable t) {
            // ignore - return null (caller will fallback/log)
        }
        return null;
    }

    // ----------------------------
    // Small extract helpers
    // ----------------------------
    private static int extractInt(Map<String, Object> m, String key, int def) {
        if (m == null) return def;
        Object o = m.get(key);
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof String) {
            try { return Integer.parseInt((String) o); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private static float extractFloat(Map<String, Object> m, String key, float def) {
        if (m == null) return def;
        Object o = m.get(key);
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).floatValue();
        if (o instanceof String) {
            try { return Float.parseFloat((String) o); } catch (NumberFormatException ignored) {}
        }
        return def;
    }
}

