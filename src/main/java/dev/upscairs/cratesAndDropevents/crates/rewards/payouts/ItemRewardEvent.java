package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import dev.upscairs.cratesAndDropevents.db.Serializer;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;

public class ItemRewardEvent implements CrateRewardEvent {

    private ItemStack item;

    public ItemRewardEvent(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public CompletableFuture<Void> execute(Player player, Location location) {

        player.getWorld().dropItemNaturally(location, item.clone());

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String asString() {
        return "item§" + Serializer.itemStackToJson(item);
    }

    public ItemStack getRenderItem() {
        ItemStack renderItem = item.clone();
        ItemMeta meta = renderItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Drop " + item.getI18NDisplayName(), "#00AAAA"));
        renderItem.setItemMeta(meta);
        return renderItem;
    }

    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_ITEM_EVENT;
    }

    public ItemRewardEvent clone() {
        return new ItemRewardEvent(item.clone());
    }

}
