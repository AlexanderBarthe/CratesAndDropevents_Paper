package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import com.google.gson.JsonObject;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class CommandRewardEvent implements CrateRewardEvent {

    private String command;
    private final Plugin plugin;

    private static final CrateRewardType TYPE = CrateRewardType.COMMAND;

    public CommandRewardEvent(String command, Plugin plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    public CommandRewardEvent(JsonObject json, Plugin plugin) {
        this.plugin = plugin;

        if (json == null) {
            throw new IllegalArgumentException("json must not be null for CommandRewardEvent");
        }

        if (!json.has("command")) {
            this.command = "";
        }
        else {
            try {
                this.command = json.get("command").getAsString();
            } catch (Exception e) {
                this.command = "";
                plugin.getLogger().warning("Invalid 'command' value in CommandRewardEvent JSON");
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public CompletableFuture<Void> execute(Player player, Location location) {

        String ESCAPED_PERCENT = "__ESCAPED_PERCENT-YINm8eZh2z7mDF4oB7Cl__";

        String resolved = command.replace("\\%", ESCAPED_PERCENT);

        resolved = resolved
                .replace("%p", player.getName())
                .replace("%w", location.getWorld().getKey().asString())
                .replace("%l", location.getX() + " " + location.getY() + " " + location.getZ());

        resolved = resolved.replace(ESCAPED_PERCENT, "%");

        String finalResolved = resolved;
        Bukkit.getScheduler().runTask(plugin,
                () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalResolved)
        );
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("/" + command, "#00AAAA"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE.name());
        obj.addProperty("command", command);
        return obj;
    }

    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_COMMAND_EVENT;
    }

    public CommandRewardEvent clone() {
        return new CommandRewardEvent(command, plugin);
    }
}
