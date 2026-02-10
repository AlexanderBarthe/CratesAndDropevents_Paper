package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import com.google.gson.JsonObject;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class SoundRewardEvent implements CrateRewardEvent {
    private String soundName;
    private float volume;
    private float pitch;

    private static final CrateRewardType TYPE = CrateRewardType.SOUND;

    public SoundRewardEvent(String soundName, float volume, float pitch) {
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundRewardEvent(JsonObject json, Plugin plugin) {
        String parsedSound = "";
        float parsedVolume = 1.0f;
        float parsedPitch = 1.0f;

        if(json != null) {
            // sound
            if (!json.has("sound") || json.get("sound").isJsonNull()) {
                plugin.getLogger().warning("SoundRewardEvent: missing 'sound' field, using empty string");
            }
            else {
                try {
                    parsedSound = json.get("sound").getAsString();
                } catch (Exception e) {
                    plugin.getLogger().warning("SoundRewardEvent: invalid 'sound' value, using empty string.");
                    parsedSound = "";
                }
            }

            // volume
            if (json.has("volume") && !json.get("volume").isJsonNull()) {
                try {
                    parsedVolume = json.get("volume").getAsFloat();
                } catch (Exception ignored) {}
            }

            // pitch
            if (json.has("pitch") && !json.get("pitch").isJsonNull()) {
                try {
                    parsedPitch = json.get("pitch").getAsFloat();
                } catch (Exception ignored) {}
            }
        }

        this.soundName = parsedSound;
        this.volume = parsedVolume;
        this.pitch = parsedPitch;
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
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TYPE.name());
        obj.addProperty("sound", soundName);
        obj.addProperty("volume", volume);
        obj.addProperty("pitch", pitch);
        return obj;
    }

    @Override
    public EditMode getAssociatedEditMode() {
        return EditMode.EDIT_SOUND_EVENT;
    }

    @Override
    public SoundRewardEvent clone() {
        return new SoundRewardEvent(soundName, volume, pitch);
    }

}
