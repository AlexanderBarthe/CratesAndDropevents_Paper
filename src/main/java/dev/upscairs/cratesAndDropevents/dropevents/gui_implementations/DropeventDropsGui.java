package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropService;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.GuiItemTemplate;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.PageGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DropeventDropsGui {

    private final CratesAndDropevents plugin;
    private final DropService dropService;

    private final Dropevent dropevent;
    private final CommandSender sender;
    private final PageGui gui;

    private int unusedChance;

    private final List<Drop> sortedDrops;

    public DropeventDropsGui(Dropevent dropevent, CommandSender sender, CratesAndDropevents plugin) {

        this.dropevent = dropevent;
        this.sender = sender;
        this.plugin = plugin;
        this.dropService = plugin.getDbServices().getDropService();
        this.unusedChance = dropService.getRemainingChanceForEvent(dropevent.getId());

        sortedDrops = new ArrayList<>(dropService.getDropsForDropevent(dropevent.getId()).stream().sorted(
                Comparator.comparingInt(Drop::getProbability).reversed()).toList());


        gui = new PageGui(new InteractableGui(new ItemDisplayGui()), sortedDrops, 0);
        configureClickReaction();

        gui.showPageInTitle(true);
        gui.setTitle("Loot Pool of " + dropevent.getNameRaw());

        placeItems();
        writeItemChances();


    }

    private void placeItems() {

        gui.placeItems();

        gui.setItem(46, GuiItemTemplate.BACK.create("To edit window"));

        gui.setItem(49, GuiItemTemplate.CREATE_NEW.create("Add drop"));


    }


    /**
     *
     * Adds Meta text, which shows the chance of a drop to listed items.
     *
     */
    private void writeItemChances() {

        List<ListableItemStack> updated = new ArrayList<>();

        //Write chances
        for (Drop drop : sortedDrops) {
            ItemStack originalItem = drop.getItem();
            int itemChance = drop.getProbability();

            ItemStack item = originalItem.clone();
            List<Component> lore = List.of(
                    InvGuiUtils.generateDefaultTextComponent("Chance: " + (itemChance / 10f) + "%", "#55FFFF")
            );
            item.lore(new ArrayList<>(lore));

            updated.add(new ListableItemStack(item));
        }

        //Create a "No drop" item if there is unused chance left.
        if (unusedChance > 0) {

            ItemStack voidItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = voidItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("No drop", "#FF5555").decoration(TextDecoration.BOLD, true));
            voidItem.setItemMeta(meta);
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(InvGuiUtils.generateDefaultTextComponent("Chance: " + ((float)unusedChance)/10f + "%", "#55FFFF"));
            voidItem.lore(lore);
            updated.add(new ListableItemStack(voidItem));

        }

        gui.setListedObjects(updated);
    }


    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {

            //Determine, which item was clicked on and if there is the non-clickable barrier item
            if(slot >= 0 && slot <= 44) {
                int selectedIndex = slot+45*gui.getPage();

                int lastAvailableIndex = gui.getListedObjects().size() - 1;

                if(unusedChance > 0f) lastAvailableIndex--;

                if(lastAvailableIndex < selectedIndex) return new PreventCloseGui();


                Drop drop = sortedDrops.get(selectedIndex).clone();

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new SingleDropGui(dropevent, drop, false, sender, plugin).getGui();

            }

            //Return to DropeventEditGui
            if(slot == 46) {
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new DropeventEditGui(dropevent, false, sender, plugin).getGui();
            }
            //Add a new drop to the Dropevent
            else if(slot == 49) {

                ItemStack newItem = new ItemStack(Material.STONE);
                ItemMeta meta = newItem.getItemMeta();
                meta.displayName(Component.text()
                        .content("Stone " + (dropService.getDropsForDropevent(dropevent.getId()).size()+1))
                        .decoration(TextDecoration.ITALIC, false)
                        .build());
                newItem.setItemMeta(meta);

                int newChance = Math.max(0, unusedChance);
                Drop drop = new Drop(0, dropevent.getId(), newChance, newItem);

                dropService.createDrop(drop, created -> {
                    if(sender instanceof Player p) {
                        McGuiFramework.getGuiSounds().playSuccessSound(p);
                        p.openInventory(new DropeventDropsGui(dropevent, sender, plugin).getGui().getInventory());
                    }
                });

                return gui;
            }

            return gui;
        });
    }

    public InventoryGui getGui() {
        return gui;
    }

}
