package dev.upscairs.cratesAndDropevents.resc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatMessageConfig {

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    private final Map<String, String> defaultMessages = new HashMap<>();

    public ChatMessageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        setupDefaults();
        load();
    }

    private void setupDefaults() {

        defaultMessages.put("crate.error.non-skull-item-selected", "<dark_red>You have to select a skull item!<reset>");
        defaultMessages.put("crate.error.name-not-found", "<dark_red>Crate not found!<reset>");
        defaultMessages.put("crate.error.already-exists", "<dark_red>A crate with that name already exists!<reset>");
        defaultMessages.put("crate.error.missing-name", "<dark_red>You must specify a name.<reset>");
        defaultMessages.put("crate.error.missing-url", "<dark_red>You must specify a url.<reset>");
        defaultMessages.put("crate.error.missing-player", "<dark_red>You must specify a player.<reset>");
        defaultMessages.put("crate.error.player-not-found", "<dark_red>Player not found.<reset>");
        defaultMessages.put("crate.error.no-crate-in-hand", "<dark_red>You need to hold the crate in your main hand.<reset>");

        defaultMessages.put("crate.success.created", "<green>Crate has been created.<reset>");
        defaultMessages.put("crate.success.value-updated", "<green>Value has been updated.<reset>");
        defaultMessages.put("crate.success.skull-updated", "<green>Crate texture has been updated.<reset>");
        defaultMessages.put("crate.success.deleted",  "<green>Crate has been deleted.<reset>");
        defaultMessages.put("crate.success.cloned",  "<green>Crate has been cloned.<reset>");

        defaultMessages.put("crate.info.type-name", "<dark_aqua>Type name of new crate. It has to be unique. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-folder", "<dark_aqua>Type name of the folder. It can be a new folder name. Use '/' to separate subfolders. Type '.' to target the root folder. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-display-name", "<dark_aqua>Type new display name. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-url", "<dark_aqua>Type URL of the skull (Format: http://textures.minecraft.net/texture/someLongCode). Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-sound", "<dark_aqua>Type the path of the desired sound. If you want a different volume or pitch then 1, use the format <white><path> <volume> <pitch> (seperated by spaces. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-command", "<dark_aqua>Type the desired command without <white>/<dark_aqua>. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-message", "<dark_aqua>Type the desired message. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("crate.info.type-canceled", "<gray>Canceled<reset>");

        defaultMessages.put("dropevent.broadcast.local.countdown", "<dark_purple>A dropevent starts here in %ts.<reset>");
        defaultMessages.put("dropevent.broadcast.local.start", "<dark_purple>The dropevent started.<reset>");
        defaultMessages.put("dropevent.broadcast.local.end", "<dark_purple>The dropevent has ended.<reset>");
        defaultMessages.put("dropevent.broadcast.global.countdown", "<dark_purple>A dropevent starts in %ts at %c (%w) %l.%p<reset>");
        defaultMessages.put("dropevent.broadcast.tp-prompt.chat", "<dark_red><bold>[Teleport]<reset>");
        defaultMessages.put("dropevent.broadcast.tp-prompt.hover", "<dark_purple>Click to teleport to %l!<reset>");

        defaultMessages.put("dropevent.success.teleported", "<dark_purple>Teleporting...<reset>");
        defaultMessages.put("dropevent.success.all-stopped", "<green>All events have been stopped.<reset>");
        defaultMessages.put("dropevent.success.created", "<green>Event has been created.<reset>");
        defaultMessages.put("dropevent.success.removed", "<green>Event has been removed.<reset>");
        defaultMessages.put("dropevent.success.setting-changed", "<green>Setting changed successfully.<reset>");
        defaultMessages.put("dropevent.success.given", "<green>Event given<reset>");
        defaultMessages.put("dropevent.success.cloned",  "<green>Dropevent has been cloned.<reset>");

        defaultMessages.put("dropevent.error.unknown-id", "<dark_red>Unknown event ID.<reset>");
        defaultMessages.put("dropevent.error.event-over", "<dark_red>This event is over.<reset>");
        defaultMessages.put("dropevent.error.not-teleportable", "<dark_red>This event cannot be teleported to.<reset>");
        defaultMessages.put("dropevent.error.already-teleported", "<dark_red>You can only port once to an event.<reset>");
        defaultMessages.put("dropevent.error.simultaneous-limit", "<dark_red>There are too many active events right now.<reset>");
        defaultMessages.put("dropevent.error.ownable", "<dark_red>You don't have the permission to start an event.<reset>");
        defaultMessages.put("dropevent.error.player-amount", "<dark_red>There must be at least %p players online to start an event.<reset>");
        defaultMessages.put("dropevent.error.missing-name", "<dark_red>You must specify a name.<reset>");
        defaultMessages.put("dropevent.error.name-not-found", "<dark_red>There is no event with that name.<reset>");
        defaultMessages.put("dropevent.error.name-already-exists", "<dark_red>There is already an event with that name.<reset>");
        defaultMessages.put("dropevent.error.name-no-spaces", "<dark_red>The name must not contain spaces.<reset>");
        defaultMessages.put("dropevent.error.setting-update-failed", "<dark_red>Failed to update setting.<reset>");
        defaultMessages.put("dropevent.error.use-no-perm", "<dark_red>You don't have permission to use this event.<reset>");
        defaultMessages.put("dropevent.error.missing-id", "<dark_red>You need to specify an id to do that.<reset>");
        defaultMessages.put("dropevent.error.forbidden-world", "<dark_red>You are not allowed to start an event in this world.<reset>");
        defaultMessages.put("dropevent.error.hopper", "<dark_red>You can't start a dropevent where hoppers could collect items.<reset>");

        defaultMessages.put("dropevent.info.sneak-for-use", "<dark_aqua>Sneak + right click to use this item.");
        defaultMessages.put("dropevent.info.type-name", "<dark_aqua>Type name of new dropevent. It has to be unique. Use /crates cancel or the button to cancel<reset>");
        defaultMessages.put("dropevent.info.type-command", "<dark_aqua>Type the desired command without <white>/<dark_aqua>. Type <white>.<dark_aqua> to clear the command. Use /de cancel or the button to cancel<reset>");
        defaultMessages.put("dropevent.info.type-folder", "<dark_aqua>Type name of the folder. It can be a new folder name. Use '/' to separate subfolders. Type '.' to target the root folder. Use /de cancel or the button to cancel<reset>");

        defaultMessages.put("system.command.error.player-not-found", "<dark_red>There is no player with that name.<reset>");
        defaultMessages.put("system.command.error.invalid-number", "<dark_red>Please specify a valid number.<reset>");
        defaultMessages.put("system.command.error.number-range-item", "<dark_red>You can only select 1 to 64 items.<reset>");
        defaultMessages.put("system.command.error.not-found", "<dark_red>Command not found<reset>");
        defaultMessages.put("system.command.error.not-enough-arguments", "<dark_red>Not enough arguments.<reset>");

    }

    public void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        // Set defaults if missing
        for (String key : defaultMessages.keySet()) {
            if (!config.contains(key)) {
                config.set(key, defaultMessages.get(key));
            }
        }

        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return config.getString(key, "");
    }

    public Component getColored(String key) {
        return MiniMessage.miniMessage().deserialize(get(key));
    }

}
