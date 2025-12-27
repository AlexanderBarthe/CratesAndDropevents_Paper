package dev.upscairs.cratesAndDropevents;

import dev.upscairs.cratesAndDropevents.cad_command.CaDCommand;
import dev.upscairs.cratesAndDropevents.dropevents.management.DropeventItemHandler;
import dev.upscairs.cratesAndDropevents.resc.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.commands.CratesCommand;
import dev.upscairs.cratesAndDropevents.crates.management.CratePlaceHandler;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.dropevents.commands.DropeventCommand;
import dev.upscairs.cratesAndDropevents.resc.DropeventStorage;
import dev.upscairs.cratesAndDropevents.helper.EventDragonDropPreventListener;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class CratesAndDropevents extends JavaPlugin {

    private static ChatMessageConfig chatMessageConfig;

    private static CratesAndDropevents instance;

    @Override
    public void onEnable() {

        instance = this;

        ConfigurationSerialization.registerClass(CrateReward.class, "CrateReward" );
        ConfigurationSerialization.registerClass(Dropevent.class, "Dropevent");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        CrateStorage.init(this);
        DropeventStorage.init(this);

        registerCommands();
        registerEvents();
        registerConfigs();

        McGuiFramework.initalize(this);
        McGuiFramework.playSounds(getConfig().getBoolean("gui.play-sounds"));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadConfigs() {
        reloadConfig();
        registerConfigs();
        CrateStorage.init(this);
        DropeventStorage.init(this);
    }

    private void registerCommands() {
        getCommand("dropevent").setExecutor(new DropeventCommand(this));
        getCommand("dropevent").setTabCompleter(new DropeventCommand(this));

        getCommand("de").setExecutor(new DropeventCommand(this));
        getCommand("de").setTabCompleter(new DropeventCommand(this));

        getCommand("crates").setExecutor(new CratesCommand(this));
        getCommand("crates").setTabCompleter(new CratesCommand(this));

        getCommand("crate").setExecutor(new CratesCommand(this));
        getCommand("crate").setTabCompleter(new CratesCommand(this));

        getCommand("cad").setExecutor(new CaDCommand(this));
        getCommand("cad").setTabCompleter(new CaDCommand(this));

    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new EventDragonDropPreventListener(), this);
        getServer().getPluginManager().registerEvents(new DropeventItemHandler(this), this);
        getServer().getPluginManager().registerEvents(new CratePlaceHandler(), this);
        getServer().getPluginManager().registerEvents(new ChatMessageInputHandler(), this);
    }

    private void registerConfigs() {
        chatMessageConfig = new ChatMessageConfig(this);
        configureDefaultConfig();
    }

    public void configureDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        saveDefaultConfig();

        setConfigDefaults();

        getConfig().options().copyDefaults(true);

        saveConfig();
    }

    private void setConfigDefaults() {
        getConfig().addDefault("dropevents.simultaneous-limit.active", false);
        getConfig().addDefault("dropevents.simultaneous-limit.count", 5);
        getConfig().addDefault("dropevents.normal-players.usable", false);
        //Removed: getConfig().addDefault("dropevents.normal-players.start.online-player-condition", false);
        //Removed: getConfig().addDefault("dropevents.normal-players.start.min-online-players", 10);
        getConfig().addDefault("dropevents.forbidden-worlds", List.of("minecraft:the_nether", "minecraft:the_end"));
        getConfig().addDefault("dropevents.hopper-prevention", false);
        getConfig().addDefault("dropevents.ops-override-restrictions", true);
        getConfig().addDefault("dropevents.bossbar-countdown", false);
        getConfig().addDefault("dropevents.starter-dragon", true);
        getConfig().addDefault("gui.play-sounds", true);

        getConfig().options().copyDefaults(true);

        saveConfig();
    }

    public ChatMessageConfig getChatMessageConfig() {
        return chatMessageConfig;
    }

    public static CratesAndDropevents getInstance() {
        return instance;
    }

}
