package dev.upscairs.cratesAndDropevents.dropevents;

import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import org.bukkit.inventory.ItemStack;

public class Drop implements ListableGuiObject {

    private int id;
    private int dropeventId;
    private int probability;
    private ItemStack item;

    public Drop(int id, int dropeventId, int probability, ItemStack item) {
        this.id = id;
        this.dropeventId = dropeventId;
        this.probability = probability;
        this.item = item;
    }

    public int getId() { return id; }
    public int getDropeventId() { return dropeventId; }
    public int getProbability() { return probability; }
    public ItemStack getItem() { return item; }

    public void setId(int id) { this.id = id; }
    public void setDropeventId(int dropeventId) { this.dropeventId = dropeventId; }
    public void setProbability(int probability) { this.probability = probability; }
    public void setItem(ItemStack item) { this.item = item; }

    public Drop clone() {
        return new Drop(id, dropeventId, probability, item.clone());
    }

    @Override
    public ItemStack getRenderItem() {
        return item;
    }
}
