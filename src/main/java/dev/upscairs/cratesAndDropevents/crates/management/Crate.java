package dev.upscairs.cratesAndDropevents.crates.management;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
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
public class Crate implements ConfigurationSerializable, ListableGuiObject {

    private String name;
    private ItemStack crateItem;
    private boolean pittySystem;

    private Map<CrateReward, Integer> rewards = new HashMap<>();


    private final Plugin plugin;

    public Crate(String name, Plugin plugin) {
        this.name = name;
        this.plugin = plugin;
        this.pittySystem = false;

        crateItem = new ItemStack(InvGuiUtils.generateCustomUrlHeadStack("http://textures.minecraft.net/texture/f1327353e2f6364b437f1e6c4a7e9764ea95e27deec0031eec1142df2f949b3"));
        crateItem.setAmount(1);
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);

        addCrateFlag();


    }

    public Crate(String name, ItemStack crateItem, boolean pittySystem, Plugin plugin) {

        this.plugin = plugin;
        this.name = name;
        this.pittySystem = pittySystem;

        crateItem.setAmount(1);
        crateItem.setType(Material.PLAYER_HEAD);

        /*
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);*/
        this.crateItem = crateItem;

        addCrateFlag();


    }

    public Crate(ItemStack crateItem, Map<CrateReward, Integer> rewards, Plugin plugin) {
        this.crateItem = crateItem;
        this.name = crateItem.getItemMeta().getDisplayName();
        this.rewards = rewards;
        this.plugin = plugin;

        addCrateFlag();
    }

    public Crate(String name, ItemStack crateItem, boolean pittySystem, Map<CrateReward, Integer> rewards, Plugin plugin) {
        this.crateItem = crateItem;
        this.name = name;
        this.pittySystem = pittySystem;
        this.rewards = rewards;
        this.plugin = plugin;

        /*
        ItemMeta meta = crateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        crateItem.setItemMeta(meta);
         */

        addCrateFlag();
    }

    private void addCrateFlag() {
        ItemMeta meta = crateItem.getItemMeta();
        meta.getPersistentDataContainer().set(
                ((CratesAndDropevents) plugin).CRATE_KEY,
                PersistentDataType.STRING,
                name);
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

    public void setPittySystem(boolean pittySystem) {
        this.pittySystem = pittySystem;
    }

    public boolean pittySystemActive() {
        return pittySystem;
    }

    public Map<CrateReward, Integer> getRewards() {
        return rewards;
    }

    public void setRewards(Map<CrateReward, Integer> rewards) {
        this.rewards = rewards;
    }

    public void setRewardChance(CrateReward reward, int chance) {
        rewards.put(reward, chance);
    }

    public void addReward(CrateReward reward, int chance) {
        rewards.put(reward, chance);
    }

    public void removeReward(CrateReward reward) {
        rewards.remove(reward);
    }

    public String getName() {
        return name;
    }

    public int getUnusedChance() {

        int summedChance = 0;

        for (Map.Entry<CrateReward, Integer> entry : rewards.entrySet()) {
            summedChance += entry.getValue();
        }

        return 1000 - summedChance;
    }

    public Crate clone() {

        Map<CrateReward, Integer> clonedRewards = new HashMap<>();
        for (Map.Entry<CrateReward, Integer> entry : rewards.entrySet()) {
            clonedRewards.put(entry.getKey().clone(), entry.getValue());
        }

        return new Crate(this.name, this.crateItem.clone(), pittySystem, clonedRewards, this.plugin);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.name);
        map.put("crateItem", crateItem);
        map.put("pittySystem", pittySystem);

        List<Map<String, Object>> rewardsList = new ArrayList<>();
        for (Map.Entry<CrateReward, Integer> entry : rewards.entrySet()) {
            Map<String, Object> rewardEntry = new LinkedHashMap<>();
            rewardEntry.put("reward", entry.getKey());
            rewardEntry.put("chance", entry.getValue());
            rewardsList.add(rewardEntry);
        }
        map.put("rewards", rewardsList);

        return map;
    }

    public static Crate deserialize(Map<String, Object> map) {
        Plugin plugin = CratesAndDropevents.getInstance();

        String name = (String) map.get("name");

        ItemStack crateItem = (ItemStack) map.get("crateItem");
        if(crateItem == null) crateItem = new ItemStack(Material.PLAYER_HEAD);

        Boolean pittySystem = (Boolean) map.get("pittySystem");
        if(pittySystem == null) pittySystem = false;

        Crate crate = new Crate(name, crateItem, pittySystem, plugin);

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
                crate.addReward(reward, chanceNum.intValue());
            }
        }

        return crate;
    }


    @Override
    public ItemStack getRenderItem() {
        return crateItem;
    }
}
