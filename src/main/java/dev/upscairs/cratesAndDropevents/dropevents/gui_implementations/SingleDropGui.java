package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import dev.upscairs.cratesAndDropevents.helper.ConfirmationGui;
import dev.upscairs.cratesAndDropevents.helper.GuiItemTemplate;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class SingleDropGui {

    private Dropevent dropevent;
    private ItemStack dropItem;
    private boolean itemSelection;
    private CommandSender sender;
    private Plugin plugin;
    private int unusedChance;

    private int currentChance;

    private InteractableGui gui;

    public SingleDropGui(Dropevent dropevent, ItemStack dropItem, boolean itemSelection, int unusedChance, CommandSender sender, Plugin plugin) {

        gui = new InteractableGui(new ItemDisplayGui());
        configureClickReaction();

        this.dropevent = dropevent;
        this.dropItem = dropItem;
        this.itemSelection = itemSelection;
        this.sender = sender;
        this.plugin = plugin;
        this.unusedChance = unusedChance;

        currentChance = dropevent.getDrops().entrySet().stream()
                .filter(e -> e.getKey().isSimilar(dropItem))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0);

        gui.setSize(54);
        gui.setTitle("Configure Loot for " + dropevent.getName());

        placeItems();
        configureClickReaction();
    }

    public void placeItems() {

        gui.setItem(45, GuiItemTemplate.BACK.create("To the overview"));

        gui.setItem(13, generateDropItem());


        ItemStack chanceItem = new ItemStack(Material.CHEST);
        ItemMeta meta = chanceItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Probability: " + currentChance/10 + "%", "#FFAA00").decoration(TextDecoration.BOLD, true));
        chanceItem.setItemMeta(meta);
        gui.setItem(29, chanceItem);

        gui.setItem(33, GuiItemTemplate.DELETE.create("Delete drop"));


    }

    private ItemStack generateDropItem() {

        ItemStack dropItem;
        ItemMeta meta;

        if(itemSelection) {
            dropItem = new ItemStack(Material.SCAFFOLDING);
            meta = dropItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click on new item or click here to abort", "#AA00AA").decoration(TextDecoration.BOLD, true));
            meta.setEnchantmentGlintOverride(true);
            dropItem.setItemMeta(meta);
        }
        else {
            dropItem = this.dropItem.clone();
            meta = dropItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click to configure drop", "#AA00AA").decoration(TextDecoration.BOLD, true));
            dropItem.setItemMeta(meta);
        }

        return dropItem;

    }

    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {

            if(slot < 54) {
                switch (slot) {
                    case 13:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new SingleDropGui(dropevent, dropItem, !itemSelection, unusedChance, sender, plugin).getGui();
                    case 29:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new DropChanceSelectionGui(dropevent, dropItem, currentChance, unusedChance, sender, plugin).getGui();
                    case 33:

                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                        return new ConfirmationGui("Delete Drop?",
                                GuiItemTemplate.DELETE.create("Delete Drop"),
                                GuiItemTemplate.BACK.create("Abort"),
                                () -> {
                            dropevent.removeDrop(dropItem);
                            DropeventStorage.saveDropevent(dropevent);
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                            return new DropeventDropsGui(dropevent, sender, plugin).getGui();
                        }, () -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return new DropeventDropsGui(dropevent, sender, plugin).getGui();
                        }).getGui();
                    case 45:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new DropeventDropsGui(dropevent, sender, plugin).getGui();
                }

                return new PreventCloseGui();

            }
            if(slot >= 54 && itemSelection) {

                dropevent.removeDrop(dropItem);
                dropevent.setItemDropChance(item, currentChance);

                DropeventStorage.saveDropevent(dropevent);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleDropGui(dropevent, item, false, unusedChance, sender, plugin).getGui();
            }

            return new PreventCloseGui();

        });
    }

    public InventoryGui getGui() {
        return gui;
    }



}
