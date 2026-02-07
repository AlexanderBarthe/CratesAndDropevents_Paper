package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import com.google.gson.JsonObject;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class MessageRewardEvent implements CrateRewardEvent {

    private String messageRaw;

    private static final CrateRewardType TYPE = CrateRewardType.MESSAGE;

    public MessageRewardEvent(String message) {
        this.messageRaw = message;
    }

    public MessageRewardEvent(JsonObject json, Plugin plugin) {
        String msg = "";

        if (json == null) {
            plugin.getLogger().warning("MessageRewardEvent: json is null, using empty message");
        } else if (!json.has("message") || json.get("message").isJsonNull()) {
            plugin.getLogger().warning("MessageRewardEvent: missing 'message' field, using empty message");
        } else {
            try {
                msg = json.get("message").getAsString();
            } catch (Exception e) {
                plugin.getLogger().warning("MessageRewardEvent: invalid 'message' value, using empty message.");
                msg = "";
            }
        }

        this.messageRaw = msg;
    }

    public String getMessageRaw() {
        return messageRaw;
    }

    public void setMessageRaw(String messageRaw) {
        this.messageRaw = messageRaw;
    }

    @Override
    public CompletableFuture<Void> execute(Player player, Location location) {

        player.sendMessage(MiniMessage.miniMessage().deserialize(messageRaw));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE.name());
        obj.addProperty("message", messageRaw);
        return obj;
    }

    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("Say " + messageRaw).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_MESSAGE_EVENT;
    }

    public MessageRewardEvent clone() {
        return new MessageRewardEvent(messageRaw);
    }
}
