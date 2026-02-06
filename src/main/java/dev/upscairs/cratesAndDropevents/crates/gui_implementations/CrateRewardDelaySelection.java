package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.DelayRewardEvent;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.NumberSelectionGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CrateRewardDelaySelection {

    private final CratesAndDropevents plugin;

    private final CommandSender sender;
    private final NumberSelectionGui gui;

    private final Crate crate;
    private final CrateReward crateReward;
    private final DelayRewardEvent delayEvent;

    public CrateRewardDelaySelection(int currentDelay, CrateReward crateReward, Crate crate, DelayRewardEvent delayEvent, CommandSender sender, CratesAndDropevents plugin) {

        gui = new NumberSelectionGui(new InteractableGui(new ItemDisplayGui()), currentDelay, 0, 1000, sender);

        this.crate = crate;
        this.crateReward = crateReward;
        this.delayEvent = delayEvent;
        this.sender = sender;
        this.plugin = plugin;

        configureClickReaction();
        gui.setTitle("Choose Delay in Ticks");

    }

    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot == 30) {
                int newDelay = gui.getNumber();
                delayEvent.setTicks(newDelay);
                plugin.getDbServices().getCrateRewardService().updateReward(crateReward);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                return new SingleRewardGui(crate, crateReward, null, EditMode.NONE, sender, plugin).getGui();
            }
            else if(slot == 32) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleRewardGui(crate, crateReward, null, EditMode.NONE, sender, plugin).getGui();
            }

            return new PreventCloseGui();
        });
    }

    public InventoryGui getGui() {
        return gui;
    }

}
