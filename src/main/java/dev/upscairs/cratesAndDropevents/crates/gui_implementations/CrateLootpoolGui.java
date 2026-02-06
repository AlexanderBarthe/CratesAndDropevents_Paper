package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.db.services.CrateRewardService;
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

    private final Crate crate;

    private List<CrateReward> rewardsSorted;

    private final CrateRewardService rewardService;


    private final PageGui gui;

    public CrateLootpoolGui(Crate crate, CratesAndDropevents plugin) {

        rewardService = plugin.getDbServices().getCrateRewardService();

        //Fetches rewards and sorts them by probability
        rewardsSorted = new ArrayList<>(
                rewardService.getRewardsForCrate(crate.getId()).stream().sorted(
                        Comparator.comparingInt(CrateReward::getProbability)
                        .reversed())
                .toList());

        gui = new PageGui(
                new InteractableGui(new ItemDisplayGui()),
                rewardsSorted, 0);

        this.crate = crate;

        gui.showPageInTitle(true);
        gui.setTitle("Lootpool of " + crate.getName());

        gui.placeItems();
        writeRewardChances();
    }

    public void writeRewardChances() {

        List<ListableItemStack> updated = new ArrayList<>();

        //Write chances
        for (CrateReward reward : rewardsSorted) {
            ItemStack originalItem = reward.getRenderItem();
            int itemChance = reward.getProbability();

            ItemStack item = originalItem.clone();
            List<Component> lore = List.of(
                    InvGuiUtils.generateDefaultTextComponent("Chance: " + (itemChance / 10f) + "%", "#55FFFF")
            );
            item.lore(new ArrayList<>(lore));

            updated.add(new ListableItemStack(item));
        }

        int unusedChance = rewardService.getRemainingChanceForCrate(crate.getId());

        //Create a "No drop" item if there is unused chance left.
        if (unusedChance > 0) {

            ItemStack voidItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = voidItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("No reward", "#FF5555"));
            voidItem.setItemMeta(meta);
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(InvGuiUtils.generateDefaultTextComponent("Chance: " + ((float)unusedChance)/10f + "%", "#55FFFF"));
            voidItem.lore(lore);
            updated.add(new ListableItemStack(voidItem));

        }


        gui.setListedObjects(updated);
    }

    public PageGui getGui() {
        return gui;
    }
}
