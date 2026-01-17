package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.PageGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableItemStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CrateLootpoolGui {

    private Crate crate;
    private CommandSender sender;
    private Plugin plugin;

    private PageGui gui;

    public CrateLootpoolGui(Crate crate, CommandSender sender, Plugin plugin) {
        gui = new PageGui(new InteractableGui(new ItemDisplayGui()),
                crate.getRewards().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .map(Map.Entry::getKey)
                        .map(reward -> new ListableItemStack(reward.getRenderItem()))
                        .toList(),
                0);

        this.crate = crate;
        this.sender = sender;
        this.plugin = plugin;

        gui.showPageInTitle(true);
        gui.setTitle("Lootpool of " + crate.getName());

        gui.placeItems();
        writeRewardChances();
    }

    public void writeRewardChances() {
        List<Map.Entry<CrateReward, Integer>> entries = new ArrayList<>(crate.getRewards().entrySet());
        //Sort by probability
        entries.sort(Map.Entry.<CrateReward, Integer>comparingByValue(Comparator.reverseOrder()));

        List<ListableItemStack> updated = new ArrayList<>();
        int summedProbability = 0;

        //Write chances
        for (Map.Entry<CrateReward, Integer> entry : entries) {
            ItemStack originalItem = entry.getKey().getRenderItem();
            int itemChance = entry.getValue();
            summedProbability += itemChance;

            ItemStack item = originalItem.clone();
            List<Component> lore = List.of(
                    InvGuiUtils.generateDefaultTextComponent("Chance: " + (itemChance / 10f) + "%", "#55FFFF")
            );
            item.lore(new ArrayList<>(lore));

            updated.add(new ListableItemStack(item));
        }


        //Create a "No drop" item if there is unused chance left.
        if (crate.getUnusedChance() > 0) {

            ItemStack voidItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = voidItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("No reward", "#FF5555"));
            voidItem.setItemMeta(meta);
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(InvGuiUtils.generateDefaultTextComponent("Chance: " + ((float)crate.getUnusedChance())/10f + "%", "#55FFFF"));
            voidItem.lore(lore);
            updated.add(new ListableItemStack(voidItem));

        }


        gui.setListedObjects(updated);
    }



    public PageGui getGui() {
        return gui;
    }
}
