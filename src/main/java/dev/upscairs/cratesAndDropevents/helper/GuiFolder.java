package dev.upscairs.cratesAndDropevents.helper;

import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiFolder implements ListableGuiObject {

    private final String folder;

    public GuiFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public ItemStack getRenderItem() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();

        String name = folder.contains("/") ? folder.substring(folder.lastIndexOf("/") + 1) : folder;

        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent(name, "#FFAA00"));
        item.setItemMeta(meta);
        return item;
    }

    public String getFolder() {
        return folder;
    }
}
