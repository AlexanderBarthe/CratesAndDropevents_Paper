package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import dev.upscairs.cratesAndDropevents.helper.EditMode;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;

public class MessageRewardEvent implements CrateRewardEvent {

    private String messageRaw;

    public MessageRewardEvent(String message) {
        this.messageRaw = message;
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
    public String asString() {
        return "message§" + messageRaw;
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
