package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateListGui;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.helper.GuiFolder;
import dev.upscairs.cratesAndDropevents.resc.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import dev.upscairs.cratesAndDropevents.resc.DropeventStorage;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.PageGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.formatter.qual.InvalidFormat;

import java.util.ArrayList;
import java.util.List;

public class DropeventListGui {

    List<ListableGuiObject> listedObjects = new ArrayList<>();

    String folder;

    CommandSender sender;

    private Plugin plugin;
    private ChatMessageConfig messageConfig;

    private PageGui gui;

    public DropeventListGui(String folder, CommandSender sender, Plugin plugin) {

        this.folder = folder;

        listedObjects.addAll(DropeventStorage.getSubfolders(folder).stream().map(GuiFolder::new).toList());
        listedObjects.addAll(DropeventStorage.getDropeventsInFolder(folder));

        gui = new PageGui(new InteractableGui(new ItemDisplayGui()), listedObjects, 0);
        configureClickReaction();

        this.sender = sender;
        this.plugin = plugin;
        this.messageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();

        gui.showPageInTitle(true);
        gui.setTitle("Dropevents" + (folder.isEmpty() ? "" : " in " + folder));

        setItems();
    }

    public void setItems() {

        ItemStack folderBackItem = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = folderBackItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Upper folder", "#AAAAAA"));
        folderBackItem.setItemMeta(meta);
        if(!folder.isEmpty()) gui.setItem(46, folderBackItem);

        ItemStack createItem = new ItemStack(Material.CHEST_MINECART);
        meta = createItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Create new dropevent", "00AAAA"));
        createItem.setItemMeta(meta);
        gui.setItem(48, createItem);

    }


    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot >= 0 && slot <= 44) {
                int selectedIndex = slot+45*gui.getPage();

                if(listedObjects.size() <= selectedIndex) {
                    return new PreventCloseGui();
                }

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                if(listedObjects.get(selectedIndex) instanceof GuiFolder f) {
                    return new DropeventListGui(f.getFolder(), sender, plugin).getGui();
                }
                else if (listedObjects.get(selectedIndex) instanceof Dropevent d) {
                    Bukkit.dispatchCommand(sender, "dropevent info " + d.getName());
                }
                return new PreventCloseGui();

            }
            else if (slot == 46) {
                if (folder.isEmpty()) return new PreventCloseGui();
                if (sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new DropeventListGui(folder.substring(0, folder.lastIndexOf("/")), sender, plugin).getGui();
            }
            else if(slot == 48) {
                Component cancelComponent = Component.text(" [Cancel]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/de cancel"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to Cancel", NamedTextColor.RED)))
                        .decorate(TextDecoration.BOLD);

                sender.sendMessage(messageConfig.getColored("dropevent.info.type-name").append(cancelComponent));

                ChatMessageInputHandler.addListener(sender, (msg) -> {
                    if (sender instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Bukkit.dispatchCommand(sender, "dropevent create " + msg + " " + folder);
                            p.openInventory(new DropeventListGui(folder, sender, plugin).getGui().getInventory());
                        });
                    }
                });

                if(sender instanceof Player p) p.closeInventory();
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return null;


            }

            return new PreventCloseGui();
        });
    }

    public InventoryGui getGui() {
        return gui;
    }


}
