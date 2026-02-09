package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import com.google.gson.JsonObject;
import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;

public class MoneyRewardEvent implements CrateRewardEvent {

    private float amount;
    private final CratesAndDropevents plugin;

    private static final CrateRewardType TYPE = CrateRewardType.MONEY;

    public MoneyRewardEvent(float amount, CratesAndDropevents plugin) {
        this.amount = amount;
        this.plugin = plugin;
    }

    public MoneyRewardEvent(JsonObject json, CratesAndDropevents plugin) {
        this.plugin = plugin;

        float amount = 0.0f;

        if(json != null) {
            try {
                amount = json.get("amount").getAsFloat();
            } catch (Exception ignored) {}
        }

        this.amount = amount;

    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public CompletableFuture<Void> execute(Player player, Location location) {

        Economy eco = plugin.getEconomy();

        if(eco == null) return CompletableFuture.completedFuture(null);

        eco.depositPlayer(player, amount);

        return CompletableFuture.completedFuture(null);

    }

    @Override
    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Pay " + amount, "#FFFF55"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE.name());
        obj.addProperty("amount", amount);
        return obj;
    }

    @Override
    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_MONEY_EVENT;
    }

    @Override
    public MoneyRewardEvent clone() {
        return new MoneyRewardEvent(amount, plugin);
    }


}
