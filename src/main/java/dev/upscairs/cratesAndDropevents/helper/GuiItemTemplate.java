package dev.upscairs.cratesAndDropevents.helper;

import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GuiItemTemplate {
    CREATE_NEW(createNewTemplate()),
    CLONE(cloneTemplate()),
    DELETE(deleteTemplate()),
    BACK(backTemplate()),
    LOOTPOOL(lootpoolTemplate()),
    FOLDER_CONFIG(folderConfigTemplate()),
    UPPER_FOLDER(upperFolderTemplate());

    private final ItemStack template;

    GuiItemTemplate(ItemStack template) {
        this.template = template;
    }

    public ItemStack create() {
        return template.clone();
    }

    public ItemStack create(String displayName) {
        ItemStack item = template.clone();
        ItemMeta meta = item.getItemMeta();

        Component originalName = template.getItemMeta().displayName();

        Component newName;
        if (originalName != null) {
            newName = Component.text(displayName).style(originalName.style());
        } else {
            newName = InvGuiUtils.generateDefaultTextComponent(displayName, "#FFAA00")
                    .decoration(TextDecoration.BOLD, true);
        }

        meta.displayName(newName);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createNewTemplate() {
        ItemStack item = new ItemStack(Material.CHEST_MINECART);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Create new", "#00AAAA"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack upperFolderTemplate() {
        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Upper folder", "#AAAAAA"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack deleteTemplate() {
        ItemStack item = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Delete", "#FF5555"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack backTemplate() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Back", "#AAAAAA"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack lootpoolTemplate() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Lootpool", "#FFAA00"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack folderConfigTemplate() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Edit folder", "#00AAAA"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack cloneTemplate() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Clone", "#55FF55"));
        item.setItemMeta(meta);
        return item;
    }


}
