package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.NumberSelectionGui;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditDropeventNumberGui {

    private Dropevent dropevent;
    private CommandSender sender;
    private String changedSetting;

    private NumberSelectionGui gui;

    public EditDropeventNumberGui(int startNumber, int minNumber, int maxNumber, Dropevent dropevent, String changedSetting, CommandSender sender) {

        gui = new NumberSelectionGui(new InteractableGui(new ItemDisplayGui()), startNumber, minNumber, maxNumber, sender);

        this.dropevent = dropevent;
        this.changedSetting = changedSetting;
        this.sender = sender;

        configureClickReaction();
        gui.setTitle("Edit " + changedSetting + " for " + dropevent.getNameRaw());
    }



    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot == 30) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                //TODO possible race condition, fix this
                Bukkit.dispatchCommand(sender, "dropevent edit " + dropevent.getId() + " "  + changedSetting + " " + gui.getNumber());
                Bukkit.dispatchCommand(sender, "dropevent info " + dropevent.getId());
            }
            else if(slot == 32) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                Bukkit.dispatchCommand(sender, "dropevent info " + dropevent.getId());
            }

            return new PreventCloseGui();
        });
    }

    public InventoryGui getGui() {
        return gui;
    }


}
