package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.helper.GuiItemTemplate;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.PageGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableItemStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CrateRewardsGui {

    private Crate crate;
    private CommandSender sender;
    private Plugin plugin;

    private PageGui gui;

    public CrateRewardsGui(Crate crate, CommandSender sender, Plugin plugin) {
        gui = new PageGui(new InteractableGui(new ItemDisplayGui()),
                crate.getRewards().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .map(Map.Entry::getKey)
                        .map(reward -> new ListableItemStack(reward.getRenderItem()))
                        .toList(),
                0);
        configureClickReaction();

        this.crate = crate;
        this.sender = sender;
        this.plugin = plugin;

        gui.showPageInTitle(true);
        gui.setTitle("Rewards of " + crate.getName());

        placeItems();
        writeRewardChances();
    }

    public void placeItems() {
        gui.placeItems();

        gui.setItem(46, GuiItemTemplate.BACK.create("To edit window"));

        gui.setItem(49, GuiItemTemplate.CREATE_NEW.create("Add reward"));
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

    public void configureClickReaction() {
        gui.onClick((slot, item, self) -> {

            //Determine, which item was clicked on and if there is the non-clickable barrier item
            if(slot >= 0 && slot <= 44) {
                int selectedIndex = slot+45*gui.getPage();

                int lastAvailableIndex = gui.getListedObjects().size() - 1;

                if(crate.getUnusedChance() > 0f) {
                    lastAvailableIndex--;
                }

                if(lastAvailableIndex < selectedIndex) {
                    return new PreventCloseGui();
                }

                //Lookup reference of reward (compare item and chance)
                List<ListableItemStack> listedObjects = gui.getListedObjects();
                ItemStack clickedReward = listedObjects.get(selectedIndex).getRenderItem();


                List<CrateReward> listedRewards = crate.getRewards()
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .map(Map.Entry::getKey)
                        .toList();

                CrateReward selectedReward = listedRewards.get(selectedIndex+45*gui.getPage());

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleRewardGui(crate,
                        selectedReward,
                        null,
                        EditMode.NONE,
                        sender,
                        plugin).getGui();
            }

            if(slot == 46) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new CrateEditGui(crate, false, sender, plugin).getGui();
            }
            //Add a new drop to the Dropevent
            else if(slot == 49) {

                CrateReward crateReward = new CrateReward(plugin);

                if(crate.getUnusedChance() > 0f) {
                    crate.addReward(crateReward, crate.getUnusedChance());
                }
                else {
                    crate.addReward(crateReward, 0);
                }

                CrateStorage.saveCrate(crate);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new CrateRewardsGui(crate, sender, plugin).getGui();
            }

            return gui;
        });
    }

    public PageGui getGui() {
        return gui;
    }
}
