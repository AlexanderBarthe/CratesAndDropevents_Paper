package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.NumberSelectionGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrateRewardChanceGui {

    private final CratesAndDropevents plugin;

    private final CommandSender sender;
    private final NumberSelectionGui gui;

    private final Crate crate;
    private final CrateReward reward;
    private final String defaultTitle;

    public CrateRewardChanceGui(int dropChance, int unusedChance, Crate crate, CrateReward reward, CommandSender sender, CratesAndDropevents plugin) {

        gui = new NumberSelectionGui(new InteractableGui(new ItemDisplayGui()), dropChance, 0, dropChance+unusedChance, sender);

        this.crate = crate;
        this.reward = reward;
        this.sender = sender;
        this.plugin = plugin;

        configureClickReaction();
        gui.onPostInternalClick(this::writeTitle);
        defaultTitle = "Configure Reward chance: ";

        writeTitle();

    }

    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot == 30) {
                int newProb = gui.getNumber();

                reward.setProbability(newProb);
                plugin.getDbServices().getCrateRewardService().updateReward(reward);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                return new SingleRewardGui(crate, reward, null, EditMode.NONE, sender, plugin).getGui();
            }
            else if(slot == 32) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleRewardGui(crate, reward, null, EditMode.NONE, sender, plugin).getGui();
            }

            return new PreventCloseGui();
        });
    }

    private void writeTitle() {
        String chanceString = ((float)gui.getNumber()/10) + "%";
        gui.setTitle(defaultTitle + chanceString);
    }

    public InventoryGui getGui() {
        return gui;
    }
}
