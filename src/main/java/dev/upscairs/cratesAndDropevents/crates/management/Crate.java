package dev.upscairs.cratesAndDropevents.crates.management;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.helper.FolderizableElement;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
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
import java.util.*;

@SerializableAs("Crate")
public class Crate extends FolderizableElement implements ListableGuiObject {

    private int id;
    private String name;
    private ItemStack crateItem;
    private boolean pitySystemActive;

    public static final NamespacedKey CRATE_KEY = new NamespacedKey(CratesAndDropevents.getInstance(),"CRATE");

    public Crate(String name, String folder) {
        super(folder);
        this.name = name;
        this.pitySystemActive = false;

        crateItem = new ItemStack(InvGuiUtils.generateCustomUrlHeadStack("http://textures.minecraft.net/texture/f1327353e2f6364b437f1e6c4a7e9764ea95e27deec0031eec1142df2f949b3"));
        crateItem.setAmount(1);
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);

        addCrateFlag();


    }

    public Crate(int id, String name, String folder, ItemStack crateItem, boolean pitySystem) {
        this(name, folder, crateItem, pitySystem);
        this.id = id;
    }
    
    public Crate(String name, String folder, ItemStack crateItem, boolean pitySystem) {

        super(folder);

        this.name = name;
        this.pitySystemActive = pitySystem;

        crateItem.setAmount(1);
        if(crateItem.getType() != Material.PLAYER_HEAD) crateItem.setType(Material.PLAYER_HEAD);

        /*
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);*/
        this.crateItem = crateItem;

        addCrateFlag();


    }

    public Crate(ItemStack crateItem) {
        this.crateItem = crateItem;
        this.name = crateItem.getItemMeta().getDisplayName();

        addCrateFlag();
    }

    private void addCrateFlag() {
        ItemMeta meta = crateItem.getItemMeta();
        meta.getPersistentDataContainer().set(CRATE_KEY, PersistentDataType.STRING, name);
        crateItem.setItemMeta(meta);
    }

    public ItemStack getCrateItem() {
        return crateItem;
    }

    public void setName(String name) {
        this.name = name;

        /*
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);
        */
    }

    public void setCrateItem(ItemStack crateItem) {

        if(crateItem.getType() != Material.PLAYER_HEAD) {
            return;
        }

        crateItem.setAmount(1);



        /*
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);*/

        this.crateItem = crateItem;
        addCrateFlag();
    }

    public boolean setCrateSkullUrl(String url) {

        SkullMeta meta = (SkullMeta)crateItem.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL(url));
        } catch (MalformedURLException ex) {
            Bukkit.getLogger().warning("Head Database seems to be down");
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

    public String getName() {
        return name;
    }

    public Crate clone() {

        return new Crate(this.name, getFolder(), this.crateItem.clone(), pitySystemActive);
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

        Crate crate = new Crate(name, folder, crateItem, pitySystem);

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
        return crateItem;
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
}
