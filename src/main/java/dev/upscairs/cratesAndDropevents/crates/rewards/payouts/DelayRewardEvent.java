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

public class DelayRewardEvent implements CrateRewardEvent {

    private int ticks;
    private final Plugin plugin;

    public DelayRewardEvent(int ticks, Plugin plugin) {
        this.ticks = ticks;
        this.plugin = plugin;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public CompletableFuture<Void> execute(Player player, Location location) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskLater(plugin, () -> future.complete(null), ticks);
        return future;
    }

    @Override
    public String asString() {
        return "delay§" + ticks;
    }

    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Wait " + (float)ticks/20 + "s", "#00AAAA"));
        item.setItemMeta(meta);
        return item;
    }

    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_DELAY_EVENT;
    }

    public DelayRewardEvent clone() {
        return new DelayRewardEvent(ticks, plugin);
    }

}
