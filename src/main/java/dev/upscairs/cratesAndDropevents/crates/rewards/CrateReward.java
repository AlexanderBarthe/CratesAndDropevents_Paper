package dev.upscairs.cratesAndDropevents.crates.rewards;

import com.google.gson.*;
import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@SerializableAs("CrateReward")
public class CrateReward implements ListableGuiObject {

    private int id;
    private int crateId;
    private int probability;
    private List<CrateRewardEvent> sequence;

    private static final Gson GSON = new GsonBuilder().create();
    private CratesAndDropevents plugin;

    public CrateReward(List<CrateRewardEvent> sequence, CratesAndDropevents plugin) {
        this.sequence = sequence;
        this.plugin = plugin;
        id = -1;
        crateId = -1;
        probability = -1;
    }

    public CrateReward(CratesAndDropevents plugin, int id, int crateId, int probability) {
        this.plugin = plugin;
        this.crateId = crateId;
        this.probability = probability;
        this.sequence = new ArrayList<>();
    }

    public CrateReward(int crateId, int probability, CratesAndDropevents plugin) {
        this.id = 0;
        this.sequence = new ArrayList<>();
        this.crateId = crateId;
        this.probability = probability;
        this.plugin = plugin;
    }

    public CrateReward(int id, int crateId, int probability, String rewardSequence, CratesAndDropevents plugin) {
        this.id = id;
        this.crateId = crateId;
        this.probability = probability;
        this.plugin = plugin;
        importSequenceFromString(rewardSequence);
    }

    public CrateReward(int id, int crateId, int probability, List<CrateRewardEvent> sequence, CratesAndDropevents plugin) {
        this.id = id;
        this.crateId = crateId;
        this.probability = probability;
        this.sequence = sequence;
        this.plugin = plugin;
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


        return new CrateReward(0, crateId, probability, clonedSequence, plugin);
    }

    public String sequenceToString() {
        JsonArray array = new JsonArray();

        for (CrateRewardEvent e : sequence) {
            array.add(e.toJson());
        }

        return GSON.toJson(array);
    }

    public void importSequenceFromString(String json) {
        List<CrateRewardEvent> sequenceList = new ArrayList<>();
        if (json == null || json.isBlank()) {
            this.sequence = sequenceList;
            return;
        }

        try {
            JsonArray array = JsonParser.parseString(json).getAsJsonArray();

            for (JsonElement el : array) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();

                if (!obj.has("type")) {
                    plugin.getLogger().warning("Reward event missing 'type' field: " + obj);
                    continue;
                }

                String typeStr = obj.get("type").getAsString();
                CrateRewardType rewardType = CrateRewardType.fromString(typeStr);

                if (rewardType == null) {
                    plugin.getLogger().warning("Unknown reward type: " + typeStr);
                    continue;
                }

                switch (rewardType) {
                    case COMMAND -> sequenceList.add(new CommandRewardEvent(obj, plugin));
                    case MESSAGE -> sequenceList.add(new MessageRewardEvent(obj, plugin));
                    case DELAY -> sequenceList.add(new DelayRewardEvent(obj, plugin));
                    case ITEM -> sequenceList.add(new ItemRewardEvent(obj, plugin));
                    case SOUND -> sequenceList.add(new SoundRewardEvent(obj, plugin));
                    case MONEY -> sequenceList.add(new MoneyRewardEvent(obj, plugin));
                    default -> plugin.getLogger().warning("Unhandled reward type: " + rewardType);
                }
            }

        } catch (JsonSyntaxException | IllegalStateException ex) {
            plugin.getLogger().warning("Failed to parse reward sequence JSON");
        }

        this.sequence = sequenceList;
    }

    @Override
    public ItemStack getRenderItem() {

        ItemRewardEvent irw = null;
        CommandRewardEvent crw = null;
        MessageRewardEvent mrw = null;
        SoundRewardEvent sre = null;
        MoneyRewardEvent mre = null;

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
            else if(evt instanceof MoneyRewardEvent) {
                mre = (MoneyRewardEvent) evt;
            }

        }

        ItemStack item = new ItemStack(Material.SCAFFOLDING);
        String name = "";

        if(irw != null) {
            item = irw.getItem().clone();
            name = "Drop " + irw.getItem().getI18NDisplayName();
        }
        else if(mre != null) {
            item = new ItemStack(Material.GOLD_INGOT);
            name = "Give money";
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
