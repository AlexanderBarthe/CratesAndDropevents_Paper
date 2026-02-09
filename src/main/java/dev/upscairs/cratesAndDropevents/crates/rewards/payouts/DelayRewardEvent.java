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

import java.util.concurrent.CompletableFuture;

public class DelayRewardEvent implements CrateRewardEvent {

    private int ticks;
    private final Plugin plugin;

    private static final CrateRewardType TYPE = CrateRewardType.DELAY;

    public DelayRewardEvent(int ticks, Plugin plugin) {
        this.ticks = ticks;
        this.plugin = plugin;
    }

    public DelayRewardEvent(JsonObject json, Plugin plugin) {
        this.plugin = plugin;

        int parsedTicks = 0;

        if (json == null) {
            plugin.getLogger().warning("DelayRewardEvent: json is null, defaulting ticks to 0");
        }
        else if (!json.has("ticks") || json.get("ticks").isJsonNull()) {
            plugin.getLogger().warning("DelayRewardEvent: missing 'ticks' field, defaulting to 0");
        }
        else {
            try {
                parsedTicks = json.get("ticks").getAsInt();
            } catch (Exception e) {
                plugin.getLogger().warning( "DelayRewardEvent: invalid 'ticks' value, defaulting to 0.");
            }
        }

        this.ticks = Math.max(0, parsedTicks);
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
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE.name());
        obj.addProperty("ticks", ticks);
        return obj;
    }

    @Override
    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Wait " + (float)ticks/20 + "s", "#00AAAA"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_DELAY_EVENT;
    }

    @Override
    public DelayRewardEvent clone() {
        return new DelayRewardEvent(ticks, plugin);
    }

}
