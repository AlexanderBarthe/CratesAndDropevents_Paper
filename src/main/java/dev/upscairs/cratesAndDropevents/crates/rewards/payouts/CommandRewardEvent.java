package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class CommandRewardEvent implements CrateRewardEvent {

    private String command;
    private final Plugin plugin;

    public CommandRewardEvent(String command, Plugin plugin) {
        this.command = command;
        this.plugin = plugin;
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
    public String asString() {
        return "command§" + command;
    }

    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_COMMAND_EVENT;
    }

    public CommandRewardEvent clone() {
        return new CommandRewardEvent(command, plugin);
    }
}
