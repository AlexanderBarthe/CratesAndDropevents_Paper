package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.helper.GuiFolder;
import dev.upscairs.cratesAndDropevents.helper.GuiItemTemplate;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.PageGui;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class DropeventListGui {

    private final CratesAndDropevents plugin;
    private final ChatMessageConfig messageConfig;
    private final DropeventService dropeventService;

    private final PageGui gui;
    private final CommandSender sender;

    private List<ListableGuiObject> listedObjects = new ArrayList<>();
    private String folder;

    public DropeventListGui(String folder, int page, CommandSender sender, CratesAndDropevents plugin) {

        this.folder = folder;
        this.sender = sender;
        this.plugin = plugin;
        this.dropeventService = plugin.getDbServices().getDropeventService();
        this.messageConfig = plugin.getChatMessageConfig();

        listedObjects.addAll(dropeventService.getSubfolders(folder).stream().map(f -> new GuiFolder(f, Dropevent.class, plugin)).toList());
        listedObjects.addAll(dropeventService.getInFolder(folder));

        gui = new PageGui(new InteractableGui(new ItemDisplayGui()), listedObjects, page);
        configureClickReaction();

        gui.showPageInTitle(true);
        gui.setTitle("Dropevents" + (folder.isEmpty() ? "" : " in " + folder));

        setItems();
    }

    public void setItems() {

        if(!folder.isEmpty())
            gui.setItem(46, GuiItemTemplate.UPPER_FOLDER.create());

        gui.setItem(48, GuiItemTemplate.CREATE_NEW.create("Create new dropevent"));

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
                    return new DropeventListGui(f.getFolder(), gui.getPage(), sender, plugin).getGui();
                }
                else if (listedObjects.get(selectedIndex) instanceof Dropevent d) {
                    Bukkit.dispatchCommand(sender, "dropevent info " + d.getId());
                }
                return new PreventCloseGui();

            }
            else if (slot == 46) {
                if (folder.isEmpty()) return new PreventCloseGui();
                if (sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new DropeventListGui(folder.substring(0, folder.lastIndexOf("/")), 0, sender, plugin).getGui();
            }
            else if(slot == 48) {

                Dropevent dropevent = new Dropevent("New dropevent", folder);

                dropeventService.create(dropevent, created -> {
                    if(sender instanceof Player p) {
                        p.openInventory(new DropeventListGui(folder, gui.getPage(), sender, plugin).getGui().getInventory());
                        McGuiFramework.getGuiSounds().playClickSound(p);
                    }
                });

                return gui;

            }

            return new PreventCloseGui();
        });
    }

    public InventoryGui getGui() {
        return gui;
    }


}
