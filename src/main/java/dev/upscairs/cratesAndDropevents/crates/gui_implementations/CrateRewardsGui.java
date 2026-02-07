package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.db.services.CrateRewardService;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CrateRewardsGui {

    private final CratesAndDropevents plugin;
    private final CrateRewardService rewardService;

    private final CommandSender sender;
    private final PageGui gui;

    private final Crate crate;

    private final List<CrateReward> rewardsSorted;

    public CrateRewardsGui(Crate crate, CommandSender sender, CratesAndDropevents plugin) {

        this.crate = crate;
        this.sender = sender;
        this.plugin = plugin;
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


        configureClickReaction();



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

    public void configureClickReaction() {
        gui.onClick((slot, item, self) -> {

            //Determine, which item was clicked on and if there is the non-clickable barrier item
            if(slot >= 0 && slot <= 44) {
                int selectedIndex = slot+45*gui.getPage();

                int lastAvailableIndex = gui.getListedObjects().size() - 1;

                if(rewardService.getRemainingChanceForCrate(crate.getId()) > 0f) {
                    lastAvailableIndex--;
                }

                if(lastAvailableIndex < selectedIndex) {
                    return new PreventCloseGui();
                }

                CrateReward selectedReward = rewardsSorted.get(selectedIndex+45*gui.getPage());

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
            //Add a new reward to the crate
            else if(slot == 49) {

                int unusedChance = rewardService.getRemainingChanceForCrate(crate.getId());
                int probability = Math.max(unusedChance, 0);

                CrateReward newReward = new CrateReward(crate.getId(), probability, plugin);

                rewardService.createReward(newReward, created -> {
                    if(sender instanceof Player p) {
                        McGuiFramework.getGuiSounds().playClickSound(p);
                        p.openInventory(new CrateRewardsGui(crate, sender, plugin).getGui().getInventory());
                    }
                });

                return gui;
            }

            return gui;
        });
    }

    public PageGui getGui() {
        return gui;
    }
}
