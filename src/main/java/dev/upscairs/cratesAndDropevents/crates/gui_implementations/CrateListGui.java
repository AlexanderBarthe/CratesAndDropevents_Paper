package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CrateListGui {

    private final CratesAndDropevents plugin;
    private final ChatMessageConfig messageConfig;
    private final CrateService crateService;

    private final List<ListableGuiObject> listedObjects = new ArrayList<>();
    private final CommandSender sender;
    private final PageGui gui;

    private final String folder;

    public CrateListGui(String folder, CommandSender sender, CratesAndDropevents plugin) {

        this.plugin = plugin;
        this.messageConfig = plugin.getChatMessageConfig();
        this.crateService = plugin.getDbServices().getCrateService();

        this.folder = folder;

        listedObjects.addAll(crateService.getSubfolders(folder).stream().map(f -> new GuiFolder(f, Crate.class, plugin)).toList());
        listedObjects.addAll(crateService.getCratesInFolder(folder));

        gui = new PageGui(new InteractableGui(new ItemDisplayGui()), listedObjects, 0);
        configureClickReaction();

        this.sender = sender;
        gui.showPageInTitle(true);
        gui.setTitle("Crates" + (folder.isEmpty() ? "" : " in " + folder));

        setItems();
    }

    private void setItems() {

        if(!folder.isEmpty())
            gui.setItem(46, GuiItemTemplate.UPPER_FOLDER.create());

        gui.setItem(48, GuiItemTemplate.CREATE_NEW.create("Create new crate"));

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
                    return new CrateListGui(f.getFolder(), sender, plugin).getGui();
                }
                else if (listedObjects.get(selectedIndex) instanceof Crate c) {
                    Bukkit.dispatchCommand(sender, "crates info " + c.getId());
                }


                return new PreventCloseGui();

            }
            else if (slot == 46) {

                if(folder.isEmpty()) return new PreventCloseGui();

                if(sender instanceof Player p)  McGuiFramework.getGuiSounds().playClickSound(p);
                return new CrateListGui(folder.substring(0, folder.lastIndexOf("/")), sender, plugin).getGui();

            }
            else if (slot == 48) {

                Crate crate = new Crate("NewCrate", folder);
                crateService.createCrate(crate, created -> {
                    if(sender instanceof Player p) {
                        McGuiFramework.getGuiSounds().playClickSound(p);
                        p.openInventory(new CrateListGui(folder, sender, plugin).getGui().getInventory());
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
