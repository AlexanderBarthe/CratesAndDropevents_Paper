package dev.upscairs.cratesAndDropevents.resc;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrateStorage {

    private static FileConfiguration config;
    private static File file;

    private static final String fileName = "crates.yml";


    public static void init(JavaPlugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
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

            if (obj instanceof Crate) {
                list.add((Crate) obj);
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

    private static void saveFile() {
        try {
            config.save(file);
        } catch (IOException ignored) {}
    }
}
