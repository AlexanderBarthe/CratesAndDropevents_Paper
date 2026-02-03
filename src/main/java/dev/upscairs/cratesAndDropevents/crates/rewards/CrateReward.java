package dev.upscairs.cratesAndDropevents.crates.rewards;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@SerializableAs("CrateReward")
public class CrateReward implements ListableGuiObject {

    private int id;
    private int crateId;
    private int probability;
    private List<CrateRewardEvent> sequence;

    private Plugin plugin;

    public CrateReward(List<CrateRewardEvent> sequence, Plugin plugin) {
        this.sequence = sequence;
        this.plugin = plugin;
        id = -1;
        crateId = -1;
        probability = -1;
    }

    public CrateReward(Plugin plugin, int id, int crateId, int probability) {
        this.plugin = plugin;
        this.crateId = crateId;
        this.probability = probability;
        this.sequence = new ArrayList<>();
    }

    public CrateReward(int id, int crateId, int probability, List<CrateRewardEvent> sequence, Plugin plugin) {
        this.sequence = new ArrayList<>(sequence);
        this.crateId = crateId;
        this.probability = probability;
        this.plugin = plugin;
    }

    public CrateReward(int id, int crateId, int probability, String rewardSequence, Plugin plugin) {
        this.id = id;
        this.crateId = crateId;
        this.probability = probability;
        this.plugin = plugin;
        importSequenceFromString(rewardSequence);
    }


    public CompletableFuture<Void> execute(Player player, Location location) {

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        for (CrateRewardEvent element : sequence) {
            chain = chain.thenCompose(v -> element.execute(player, location));
        }

        return chain;
    }

    public List<CrateRewardEvent> getSequence() {
        return sequence;
    }

    public void addEvent(CrateRewardEvent event) {
        sequence.add(event);
    }

    public CrateReward clone() {

        List<CrateRewardEvent> clonedSequence = new ArrayList<>();
        for  (CrateRewardEvent element : sequence) {
            clonedSequence.add(element.clone());
        }


        return new CrateReward(clonedSequence, plugin);
    }

    public String sequenceToString() {
        StringBuilder sb = new StringBuilder();
        for (CrateRewardEvent element : sequence) {
            sb.append(element.asString()).append("§§");
        }
        return sb.toString();
    }

    public void importSequenceFromString(String string) {
        String[] sequenceStrings = string.split("§§");

        List<CrateRewardEvent> sequenceList = new ArrayList<>();

        for(String s : sequenceStrings) {
            if(s.isEmpty()) continue;

            String[] rewardData = s.split("§");

            if(rewardData.length <= 1) continue;

            switch (rewardData[0]) {
                case "command": sequenceList.add(new CommandRewardEvent(rewardData[1], plugin)); break;
                case "message": sequenceList.add(new MessageRewardEvent(rewardData[1])); break;
                case "delay": sequenceList.add(new DelayRewardEvent(Integer.parseInt(rewardData[1]), plugin)); break;
                case "item": sequenceList.add(new ItemRewardEvent(new ItemStack(Material.valueOf(rewardData[1])))); break;
                case "sound": {
                    if(rewardData.length < 4) continue;
                    sequenceList.add(new SoundRewardEvent(rewardData[1], Float.parseFloat(rewardData[2]), Float.parseFloat(rewardData[3])));
                    break;
                }
                default: continue;
            }

        }

        sequence = sequenceList;
    }

    public static CrateReward deserialize(Map<String, Object> map) {

        Plugin plugin = CratesAndDropevents.getInstance();

        Object eventsObj = map.get("events");
        if (!(eventsObj instanceof List<?> rawEventsList)) {
            throw new IllegalArgumentException("Missing or invalid 'events' for CrateReward");
        }

        List<CrateRewardEvent> seq = new ArrayList<>();
        for (Object o : rawEventsList) {
            if (!(o instanceof Map<?, ?> m)) continue;
            String type = (String) m.get("type");
            switch (type) {
                case "command":
                    seq.add(new CommandRewardEvent((String) m.get("command"), plugin));
                    break;
                case "message":
                    seq.add(new MessageRewardEvent((String) m.get("message")));
                    break;
                case "delay":
                    seq.add(new DelayRewardEvent(((Number) m.get("ticks")).intValue(), plugin));
                    break;
                case "item":
                    seq.add(new ItemRewardEvent((ItemStack) m.get("item")));
                    break;
                case "sound":
                    seq.add(new SoundRewardEvent(
                            (String) m.get("soundName"),
                            ((Number) m.get("volume")).floatValue(),
                            ((Number) m.get("pitch")).floatValue()
                    ));
                    break;
            }
        }

        return new CrateReward(seq, plugin);
    }


    @Override
    public ItemStack getRenderItem() {

        ItemRewardEvent irw = null;
        CommandRewardEvent crw = null;
        MessageRewardEvent mrw = null;
        SoundRewardEvent sre = null;

        int moreEvents = sequence.size() - 1;

        for(CrateRewardEvent evt : sequence.reversed()) {
            if(evt instanceof ItemRewardEvent) {
                irw = (ItemRewardEvent) evt;
            }
            else if(evt instanceof CommandRewardEvent) {
                crw = (CommandRewardEvent) evt;
            }
            else if(evt instanceof MessageRewardEvent) {
                mrw = (MessageRewardEvent) evt;
            }
            else if(evt instanceof SoundRewardEvent) {
                sre = (SoundRewardEvent) evt;
            }

        }

        ItemStack item = new ItemStack(Material.SCAFFOLDING);
        String name = "";

        if(irw != null) {
            item = irw.getItem().clone();
            name = "Drop " + irw.getItem().getI18NDisplayName();
        }
        else if(crw != null) {
            item = new ItemStack(Material.COMMAND_BLOCK);
            name = "Run command";
        }
        else if(mrw != null) {
            item = new ItemStack(Material.PAPER);
            name = "Send message";
        }
        else if(sre != null) {
            item = new ItemStack(Material.NOTE_BLOCK);
            name = "Play sound";
        }


        if(name.isEmpty()) {
            name = "Empty";
        }
        else {
            if(moreEvents > 0) name += " + " + moreEvents + " more";
        }

        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent(name, "#FFAA00"));

        item.setItemMeta(meta);
        return item;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCrateId() {
        return crateId;
    }

    public void setCrateId(int crateId) {
        this.crateId = crateId;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }
}
