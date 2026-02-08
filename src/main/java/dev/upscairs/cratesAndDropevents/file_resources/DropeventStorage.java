package dev.upscairs.cratesAndDropevents.file_resources;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DbServices;
import dev.upscairs.cratesAndDropevents.db.services.DropService;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

public abstract class DropeventStorage {

    private static FileConfiguration config;
    private static File file;

    private static final String fileName = "dropevents.yml";

    public static void init(JavaPlugin plugin) {

        //Create folder, if not existing
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), fileName);

        boolean createSample = false;

        if (!file.exists()) {
            try {
                file.createNewFile();
                createSample = true;
            } catch (IOException ignored) {}
        }
        //config = YamlConfiguration.loadConfiguration(file);
        //if(createSample) saveDropevent(createExampleCrate());
    }


    /**

    public static void saveDropevent(Dropevent dropevent) {
        config.set("events." + dropevent.getName(), dropevent);
        saveFile();
    }

    /**
     *
    public static void removeDropevent(Dropevent dropevent) {
        config.set("events." + dropevent.getName(), null);
        saveFile();
    }

    /**
     *
    public static List<String> getDropeventNames() {
        ConfigurationSection section = config.getConfigurationSection("events");
        List<String> keys = new ArrayList<>();

        if (section != null) {
            keys.addAll(section.getKeys(false));
        }
        return keys;
    }*/


    public static List<Dropevent> getAll() {
        List<Dropevent> list = new ArrayList<>();

        if (config.contains("events")) {
            ConfigurationSection section = config.getConfigurationSection("events");
            for (String key : section.getKeys(false)) {
                Object obj = config.get("events." + key);
                if (obj instanceof Dropevent dropevent) {

                    //Fix if render item is corrupted
                    if(dropevent.getRenderItem() == null) dropevent.setItem(new ItemStack(Material.FIREWORK_ROCKET));

                    list.add(dropevent);
                }
            }
        }
        return list;
    }

    public static void migrate(DbServices dbServices, CratesAndDropevents plugin) {

        DropeventService dropeventService = dbServices.getDropeventService();
        DropService dropService = dbServices.getDropService();

        if (config == null) {
            plugin.getLogger().warning("Migration aborted: config is null.");
            return;
        }

        ConfigurationSection eventsSection = config.getConfigurationSection("events");
        if (eventsSection == null) {
            plugin.getLogger().info("No 'events' section found in config — nothing to migrate.");
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String key : eventsSection.getKeys(false)) {
                ConfigurationSection evSec = eventsSection.getConfigurationSection(key);
                if (evSec == null) continue;

                try {
                    String folder = evSec.getString("folder", "");
                    ItemStack renderItem = readItemSafely(evSec, "renderItem");
                    if (renderItem == null) renderItem = new ItemStack(Material.FIREWORK_ROCKET);

                    int dropRange = evSec.getInt("dropRange", 100);
                    int eventTimeSec = evSec.getInt("eventTimeSec", 120);
                    int dropCount = evSec.getInt("dropCount", 1);
                    int countdownSec = evSec.getInt("countdownSec", 60);
                    boolean broadcast = evSec.getBoolean("broadcast", false);
                    boolean teleportable = evSec.getBoolean("teleportable", false);
                    String startupCommand = evSec.getString("startupCommand", "");
                    int minPlayers = evSec.getInt("minPlayers", 0);

                    Dropevent dropevent = new Dropevent(
                            0,
                            folder,
                            renderItem,
                            dropRange,
                            eventTimeSec,
                            dropCount,
                            countdownSec,
                            broadcast,
                            teleportable,
                            startupCommand,
                            minPlayers
                    );

                    dropeventService.create(dropevent, createdEvent -> {
                        if (createdEvent == null) {
                            plugin.getLogger().warning("Created dropevent callback returned null for key: " + key);
                            return;
                        }

                        // drops als Liste
                        List<?> dropsList = evSec.getList("drops");
                        if (dropsList == null || dropsList.isEmpty()) return;

                        for (int i = 0; i < dropsList.size(); i++) {
                            try {
                                ItemStack dropItem = readItemSafely(evSec, "drops." + i + ".item");
                                if (dropItem == null) {
                                    plugin.getLogger().warning("Skipping drop with missing item for event " + key + " index " + i);
                                    continue;
                                }
                                int chance = evSec.getInt("drops." + i + ".chance", evSec.getInt("drops." + i + ".probability", 100));
                                Drop drop = new Drop(0, createdEvent.getId(), chance, dropItem);
                                dropService.createDrop(drop, null);
                            } catch (Throwable t) {
                            }
                        }
                    });

                } catch (Throwable t) {
                }
            }

            plugin.getLogger().info("Dropevent migration from YAML scheduled (DB inserts run async via DAOs).");
        });
    }

    private static ItemStack readItemSafely(ConfigurationSection evSec, String path) {
        try {
            if (evSec.isSet(path)) {
                Object raw = evSec.get(path);
                if (raw instanceof ItemStack) {
                    return ((ItemStack) raw).clone();
                }
                if (raw instanceof Map) {
                    try {
                        Object des = ConfigurationSerialization.deserializeObject((Map<String, Object>) raw);
                        if (des instanceof ItemStack) return ((ItemStack) des).clone();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}




    /**

    public static Dropevent getDropeventByName(String name) {
        if (config.contains("events." + name)) {
            Object obj = config.get("events." + name);
            if (obj instanceof Dropevent) {
                return (Dropevent) obj;
            }
        }
        return null;
    }

    public static List<Dropevent> getDropeventsInFolder(String folderPath) {
        return getAll().stream().filter(de -> de.getFolder().equals(folderPath)).toList();
    }

    public static Set<String> getAllFolderPaths() {
        return getAll().stream().map(FolderizableElement::getFolder).collect(Collectors.toSet());
    }

    public static Set<String> getSubfolders(String folderPath) {

        Set<String> subfolders = new HashSet<>();

        int subfolderDepth = folderPath.split("/").length;

        for (String currentFolderPath : getAllFolderPaths()) {
            if (!currentFolderPath.startsWith(folderPath)) continue;

            String[] path = currentFolderPath.split("/");
            if (path.length > subfolderDepth) {
                String subfolderName = path[subfolderDepth];
                subfolders.add(folderPath + "/" + subfolderName);
            }
        }
        return subfolders;
    }


        /**
         *
         * Saves the config file to disk.
         *
         */

    /*
    private static Dropevent createExampleCrate() {

        HashMap<ItemStack, Integer> drops = new HashMap<>();

        Crate sampleCrate = CrateStorage.getCrateById("SampleCrate");
        ItemStack drop = sampleCrate != null ? sampleCrate.getCrateItem() : new ItemStack(Material.DIAMOND);

        drops.put(drop, 1000);

        return new Dropevent(
                "SampleEvent", "",
                new ItemStack(Material.FIREWORK_ROCKET),
                50,
                60,
                drops,
                240,
                20,
                true,
                true,
                null,
                0);
    }*/

