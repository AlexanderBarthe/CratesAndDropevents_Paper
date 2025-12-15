package dev.upscairs.cratesAndDropevents.resc;

import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropeventStorage {

    private static FileConfiguration config;
    private static File file;

    private static final String fileName = "dropevents.yml";

    public static void init(JavaPlugin plugin) {

        //Create folder, if not existing
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     *
     * Saves a dropevent to the config file. If the dropevent already exists, it will be overwritten.
     * The name of the dropevent will be used as the key in the config file.
     *
     * @param dropevent
     */
    public static void saveDropevent(Dropevent dropevent) {

        //Catching old versions TODO remove later
        if(dropevent.getMinPlayers() == -1){
            if (config.getBoolean("dropevents.normal-players.start.online-player-condition")) {
                dropevent.setMinPlayers(config.getInt("dropevents.normal-players.start.min-online-players"));
            }
            else {
                dropevent.setMinPlayers(0);
            }
        }

        //Saving
        config.set("events." + dropevent.getName(), dropevent);
        saveFile();
    }

    /**
     *
     * Deletes a dropevent from the file.
     *
     * @param dropevent
     */
    public static void removeDropevent(Dropevent dropevent) {
        config.set("events." + dropevent.getName(), null);
        saveFile();
    }

    /**
     *
     * Returns a list of all dropevent names in the config file. Suprise!
     *
     * @return
     */
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
     * Returns a list of all dropevents in the config file.
     *
     * @return
     */
    public static List<Dropevent> getAll() {
        List<Dropevent> list = new ArrayList<>();

        if (config.contains("events")) {
            ConfigurationSection section = config.getConfigurationSection("events");
            for (String key : section.getKeys(false)) {
                Object obj = config.get("events." + key);
                if (obj instanceof Dropevent) {
                    list.add((Dropevent) obj);
                }
            }
        }
        return list;
    }

    /**
     *
     * Looks up a dropevent by its name in the config file. Returns null if the dropevent does not exist.
     *
     * @param name
     * @return
     */
    public static Dropevent getDropeventByName(String name) {
        if (config.contains("events." + name)) {
            Object obj = config.get("events." + name);
            if (obj instanceof Dropevent) {
                return (Dropevent) obj;
            }
        }
        return null;
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
}
