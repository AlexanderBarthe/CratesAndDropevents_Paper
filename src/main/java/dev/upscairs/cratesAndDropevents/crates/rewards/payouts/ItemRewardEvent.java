package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import com.google.gson.JsonObject;
import dev.upscairs.cratesAndDropevents.helper.Serializer;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class ItemRewardEvent implements CrateRewardEvent {

    private ItemStack item;

    private static final CrateRewardType TYPE = CrateRewardType.ITEM;

    public ItemRewardEvent(ItemStack item) {
        this.item = item;
    }

    public ItemRewardEvent(JsonObject json, Plugin plugin) {
        ItemStack parsed = null;

        if (json == null) {
            plugin.getLogger().warning("ItemRewardEvent: json is null, using stone item");
        }
        else if (!json.has("item") || json.get("item").isJsonNull()) {
            plugin.getLogger().warning("ItemRewardEvent: missing 'item' field, using stone item");
        }
        else {
            try {
                String itemJson = json.get("item").getAsString();
                parsed = Serializer.jsonToItemStack(itemJson);
            } catch (Exception e) {
                plugin.getLogger().warning("ItemRewardEvent: failed to parse item JSON, using stone item.");
            }
        }

        if(parsed == null || parsed.getType() == Material.AIR) parsed = new ItemStack(Material.STONE);

        this.item = parsed;
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
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE.name());
        obj.addProperty("item", Serializer.itemStackToJson(item));
        return obj;
    }

    public ItemStack getRenderItem() {
        ItemStack renderItem = item.clone();
        ItemMeta meta = renderItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Drop " + item.getI18NDisplayName(), "#00AAAA"));
        renderItem.setItemMeta(meta);
        return renderItem;
    }

    @Override
    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_ITEM_EVENT;
    }

    @Override
    public ItemRewardEvent clone() {
        return new ItemRewardEvent(item.clone());
    }

}
