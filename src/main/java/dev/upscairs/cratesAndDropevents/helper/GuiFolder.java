package dev.upscairs.cratesAndDropevents.helper;

import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiFolder implements ListableGuiObject {

    private final String folder;
    private final Class<?> primaryListedObjectType;

    public GuiFolder(String folder, Class<?> primaryListedObjectType) {
        this.folder = folder;
        this.primaryListedObjectType = primaryListedObjectType;
    }

    @Override
    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();

        String name = folder.contains("/") ? folder.substring(folder.lastIndexOf("/") + 1) : folder;

        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent(name, "#FFAA00"));
        item.setItemMeta(meta);

        int elementCount = -1;

        if(primaryListedObjectType == Crate.class) {
            elementCount = 0;
            elementCount += CrateStorage.getSubfolders(folder).size();
            elementCount += CrateStorage.getCratesInFolder(folder).size();
        }
        else if(primaryListedObjectType == Dropevent.class) {
            elementCount = 0;
            elementCount += DropeventStorage.getSubfolders(folder).size();
            elementCount += DropeventStorage.getDropeventsInFolder(folder).size();
        }

        if(elementCount != -1) {
            List<Component> lore = List.of(
                    InvGuiUtils.generateDefaultTextComponent(
                            elementCount + " Element" + (elementCount != 1 ? "s" : ""),
                            "#AAAAAA")
            );
            item.lore(new ArrayList<>(lore));
        }

        return item;
    }

    public String getFolder() {
        return folder;
    }
}
