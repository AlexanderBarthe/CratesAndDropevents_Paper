package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropService;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
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

public class DropChanceSelectionGui {

    private final CratesAndDropevents plugin;
    private final DropService dropService;

    private final Dropevent dropevent;
    private final Drop drop;
    private final int unusedChance;

    private final CommandSender sender;
    private final NumberSelectionGui gui;
    private final String defaultTitle;

    public DropChanceSelectionGui(Dropevent dropevent, Drop drop, CommandSender sender, CratesAndDropevents plugin) {

        this.dropevent = dropevent;
        this.drop = drop;
        this.sender = sender;
        this.plugin = plugin;
        this.dropService = plugin.getDbServices().getDropService();

        this.unusedChance = dropService.getRemainingChanceForEvent(dropevent.getId());

        gui = new NumberSelectionGui(new InteractableGui(new ItemDisplayGui()), drop.getProbability(), 0, drop.getProbability()+unusedChance, sender);
        configureClickReaction();
        gui.onPostInternalClick(() -> writeTitle());



        defaultTitle = "Configure Drop chance: ";
        writeTitle();
    }

    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot == 30) {

                drop.setProbability(gui.getNumber());
                dropService.updateDrop(drop);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                return new DropeventDropsGui(dropevent, sender, plugin).getGui();
            }
            else if(slot == 32) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleDropGui(dropevent, drop, false, sender, plugin).getGui();
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
