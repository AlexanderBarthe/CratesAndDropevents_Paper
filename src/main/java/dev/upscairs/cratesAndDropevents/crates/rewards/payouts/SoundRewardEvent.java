package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;

public class SoundRewardEvent implements CrateRewardEvent {
    private String soundName;
    private float volume;
    private float pitch;

    public SoundRewardEvent(String soundName, float volume, float pitch) {
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getSoundName() {
        return soundName;
    }

    public void setSound(String soundName) {
        this.soundName = soundName;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public CompletableFuture<Void> execute(Player player, Location location) {
        player.playSound(player.getLocation(), soundName, SoundCategory.BLOCKS, volume, pitch);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ItemStack getRenderItem() {
        ItemStack renderItem = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta meta = renderItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Play Sound " + soundName, "#00AAAA"));
        renderItem.setItemMeta(meta);
        return renderItem;
    }

     @Override
     public String asString() {
        return "sound§" + soundName + "§" + volume + "§" + pitch;
     }

    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_SOUND_EVENT;
    }

    public SoundRewardEvent clone() {
        return new SoundRewardEvent(soundName, volume, pitch);
    }

}
