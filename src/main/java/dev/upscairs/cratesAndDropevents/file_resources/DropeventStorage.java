package dev.upscairs.cratesAndDropevents.file_resources;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
    }

    /**
     *
    public static List<Dropevent> getAll() {
        List<Dropevent> list = new ArrayList<>();

        if (config.contains("events")) {
            ConfigurationSection section = config.getConfigurationSection("events");
            for (String key : section.getKeys(false)) {
                Object obj = config.get("events." + key);
                if (obj instanceof Dropevent dropevent) {

                    //Fix if render item is corrupted
                    if(dropevent.getRenderItem() == null) dropevent.setRenderItem(new ItemStack(Material.FIREWORK_ROCKET));

                    list.add(dropevent);
                }
            }
        }
        return list;
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
    public static void saveFile() {
        try {
            config.save(file);
        } catch (IOException ignored) {}
    }

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
}
