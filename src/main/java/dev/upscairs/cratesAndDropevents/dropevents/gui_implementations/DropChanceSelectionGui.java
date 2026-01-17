package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.NumberSelectionGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class DropChanceSelectionGui {

    private Dropevent dropevent;
    private ItemStack drop;
    private int unusedChance;
    private CommandSender sender;
    private Plugin plugin;

    private NumberSelectionGui gui;

    private String defaultTitle;

    public DropChanceSelectionGui(Dropevent dropevent, ItemStack changedDrop, int dropChance, int unusedChance, CommandSender sender, Plugin plugin) {

        gui = new NumberSelectionGui(new InteractableGui(new ItemDisplayGui()), dropChance, 0, dropChance+unusedChance, sender);
        configureClickReaction();
        gui.onPostInternalClick(() -> writeTitle());

        this.dropevent = dropevent;
        this.drop = changedDrop;
        this.unusedChance = unusedChance;
        this.sender = sender;
        this.plugin = plugin;

        defaultTitle = "Configure Drop chance: ";
        writeTitle();
    }

    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot == 30) {
                dropevent.setItemDropChance(drop, gui.getNumber());
                DropeventStorage.saveDropevent(dropevent);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                return new DropeventDropsGui(dropevent, sender, plugin).getGui();
            }
            else if(slot == 32) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleDropGui(dropevent, drop, false, unusedChance, sender, plugin).getGui();
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
