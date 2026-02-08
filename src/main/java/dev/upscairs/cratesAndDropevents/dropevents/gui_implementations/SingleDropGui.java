package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropService;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
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

public class SingleDropGui {

    private final CratesAndDropevents plugin;
    private final DropService dropService;


    private final Dropevent dropevent;
    private final Drop drop;
    private final boolean itemSelection;
    private final CommandSender sender;

    private final int unusedChance;

    private final int currentChance;

    private final InteractableGui gui;

    public SingleDropGui(Dropevent dropevent, Drop drop, boolean itemSelection, CommandSender sender, CratesAndDropevents plugin) {
        this.plugin = plugin;
        this.dropService = plugin.getDbServices().getDropService();
        this.dropevent = dropevent;
        this.drop = drop;
        this.itemSelection = itemSelection;
        this.sender = sender;
        this.unusedChance = dropService.getRemainingChanceForEvent(dropevent.getId());
        this.currentChance = drop.getProbability();

        gui = new InteractableGui(new ItemDisplayGui());
        configureClickReaction();

        gui.setSize(54);
        gui.setTitle("Configure Loot for " + dropevent.getNameRaw());

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

        gui.setItem(31, GuiItemTemplate.CLONE.create("Clone drop"));

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
            dropItem = drop.getItem().clone();
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
                        return new SingleDropGui(dropevent, drop, !itemSelection, sender, plugin).getGui();
                    case 29:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new DropChanceSelectionGui(dropevent, drop, sender, plugin).getGui();
                    case 31:
                        int remainingChance = dropService.getRemainingChanceForEvent(dropevent.getId());
                        Drop clone = drop.clone();
                        clone.setProbability(Math.min(remainingChance, clone.getProbability()));

                        dropService.createDrop(clone, created -> {
                            if(sender instanceof Player p) {
                                McGuiFramework.getGuiSounds().playSuccessSound(p);
                                p.openInventory(new DropeventDropsGui(dropevent, sender, plugin).getGui().getInventory());
                            }
                        });

                        return gui;

                    case 33:

                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                        return new ConfirmationGui("Delete Drop?",
                                GuiItemTemplate.DELETE.create("Delete Drop"),
                                GuiItemTemplate.BACK.create("Abort"),
                                () -> {
                            dropService.deleteDropById(drop.getId());
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

                drop.setItem(item);
                dropService.updateDrop(drop);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleDropGui(dropevent, drop, false, sender, plugin).getGui();
            }

            return new PreventCloseGui();

        });
    }

    public InventoryGui getGui() {
        return gui;
    }



}
