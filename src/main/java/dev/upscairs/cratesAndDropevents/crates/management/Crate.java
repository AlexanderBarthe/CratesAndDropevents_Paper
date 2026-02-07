package dev.upscairs.cratesAndDropevents.crates.management;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.helper.FolderizableElement;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SerializableAs("Crate")
public class Crate extends FolderizableElement implements ListableGuiObject {

    private int id;
    private ItemStack crateItem;
    private boolean pitySystemActive;

    public static final NamespacedKey CRATE_KEY = new NamespacedKey(CratesAndDropevents.getInstance(),"CRATE_ID");

    public Crate(String name, String folder) {
        super(folder);
        this.pitySystemActive = false;

        crateItem = new ItemStack(InvGuiUtils.generateCustomUrlHeadStack("http://textures.minecraft.net/texture/f1327353e2f6364b437f1e6c4a7e9764ea95e27deec0031eec1142df2f949b3"));
        crateItem.setAmount(1);
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);

    }

    public Crate(int id, String folder, ItemStack crateItem, boolean pitySystem) {
        this(folder, crateItem, pitySystem);
        this.id = id;
    }
    
    public Crate(String folder, ItemStack crateItem, boolean pitySystem) {

        super(folder);

        this.pitySystemActive = pitySystem;

        crateItem.setAmount(1);
        if(crateItem.getType() != Material.PLAYER_HEAD) crateItem.setType(Material.PLAYER_HEAD);

        this.crateItem = crateItem;

    }

    public Crate(ItemStack crateItem) {
        this.crateItem = crateItem;
    }

    public ItemStack getCrateItem() {
        ItemMeta meta = crateItem.getItemMeta();
        meta.getPersistentDataContainer().set(CRATE_KEY, PersistentDataType.INTEGER, id);

        ItemStack item = crateItem.clone();
        item.setItemMeta(meta);
        return item;
    }

    public void setCrateItem(ItemStack crateItem) {

        if(crateItem.getType() != Material.PLAYER_HEAD) return;

        crateItem.setAmount(1);

        this.crateItem = crateItem;
    }

    public boolean setCrateSkullUrl(String url) {

        SkullMeta meta = (SkullMeta)crateItem.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL(url));
        } catch (MalformedURLException ex) {
            Bukkit.getLogger().warning("URL is invalid or the head database is down.");
            return false;
        }

        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
        crateItem.setItemMeta(meta);
        return true;
    }

    public void setPitySystemActive(boolean pitySystemActive) {
        this.pitySystemActive = pitySystemActive;
    }

    public boolean pitySystemActive() {
        return pitySystemActive;
    }

    public Crate clone() {

        return new Crate(getFolder(), this.crateItem.clone(), pitySystemActive);
    }

    public static Crate deserialize(Map<String, Object> map) {
        Plugin plugin = CratesAndDropevents.getInstance();

        String name = (String) map.get("name");

        ItemStack crateItem = (ItemStack) map.get("crateItem");
        if(crateItem == null) crateItem = new ItemStack(Material.PLAYER_HEAD);

        Boolean pitySystem = (Boolean) map.get("pittySystem");
        if(pitySystem == null) pitySystem = false;

        String folder = (String) map.get("folder");
        if(folder == null) folder = "";

        Crate crate = new Crate(folder, crateItem, pitySystem);

        Object obj = map.get("rewards");
        if (obj instanceof List<?> list) {
            for (Object element : list) {
                if (!(element instanceof Map<?, ?> rewardMap)) continue;

                Object rawReward = rewardMap.get("reward");
                CrateReward reward;
                if (rawReward instanceof CrateReward cr) {
                    reward = cr;
                } else if (rawReward instanceof Map<?, ?> serializedReward) {
                    reward = (CrateReward) ConfigurationSerialization
                            .deserializeObject((Map<String, Object>) serializedReward);
                } else {
                    continue;
                }

                Number chanceNum = (Number) rewardMap.get("chance");
            }
        }

        return crate;
    }


    @Override
    public ItemStack getRenderItem() {
        ItemStack renderItem = crateItem.clone();
        ItemMeta meta = renderItem.getItemMeta();
        List<Component> lore = meta.lore();
        if(lore == null) lore = new ArrayList<>();
        lore.add(InvGuiUtils.generateDefaultTextComponent("Id: " + id, "#555555"));
        meta.lore(lore);
        renderItem.setItemMeta(meta);

        renderItem.setAmount(1);

        return renderItem;
    }

    public void setRenderItem(ItemStack renderItem) {
        if(!renderItem.getType().equals(Material.PLAYER_HEAD)) return;
        this.crateItem = renderItem;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public Component getName() {
        return crateItem.getItemMeta().displayName();
    }

    public String getNameRaw() {
        return PlainTextComponentSerializer.plainText().serialize(getName());
    }

    public void setName(Component name) {
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(name);
        crateItem.setItemMeta(meta);
    }

    public void setName(String componentString) {
        setName(MiniMessage.miniMessage().deserialize(componentString));
    }

}
