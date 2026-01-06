package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.helper.ConfirmationGui;
import dev.upscairs.cratesAndDropevents.resc.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.InventoryGui;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Supplier;

import static dev.upscairs.cratesAndDropevents.helper.EditMode.NONE;

public class CrateEditGui {

    private Crate crate;
    private CommandSender sender;
    private Plugin plugin;
    private ChatMessageConfig messageConfig;

    private boolean crateItemSelection;

    private InteractableGui gui;

    public CrateEditGui(Crate crate, boolean crateItemSelection, CommandSender sender, Plugin plugin) {

        gui = new InteractableGui(new ItemDisplayGui());
        configureClickReaction();


        this.crate = crate;
        this.sender = sender;
        this.crateItemSelection = crateItemSelection;
        this.plugin = plugin;
        this.messageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();

        gui.setTitle("Edit " + crate.getName());
        gui.setSize(54);

        placeItems();

    }

    public void placeItems() {

        ItemMeta meta;

        ItemStack giveItem = crate.getCrateItem().clone();
        giveItem.setAmount(64);
        meta = giveItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Give crates", "#55FFFF"));
        giveItem.setItemMeta(meta);
        gui.setItem(4, giveItem);

        ItemStack deleteItem = new ItemStack(Material.LAVA_BUCKET);
        meta = deleteItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Delete crate", "#FF5555"));
        deleteItem.setItemMeta(meta);
        gui.setItem(53, deleteItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        meta = backItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("To the overview", "#AAAAAA").decoration(TextDecoration.BOLD, true));
        backItem.setItemMeta(meta);
        gui.setItem(46, backItem);

        ItemStack lootItem = new ItemStack(Material.CHEST);
        meta = lootItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Configure Rewards", "#FFAA00").decoration(TextDecoration.BOLD, true));
        lootItem.setItemMeta(meta);
        gui.setItem(31, lootItem);

        ItemStack urlItem = new ItemStack(Material.WRITABLE_BOOK);
        meta = urlItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click to edit Skull Url", "#FFAA00").decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(InvGuiUtils.generateDefaultTextComponent("Or use /crates url <crate-name> <url>", "#AA00AA")));
        urlItem.setItemMeta(meta);
        gui.setItem(22, urlItem);

        ItemStack cloneItem = new ItemStack(Material.EMERALD);
        meta = cloneItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Clone Crate", "#55FF55"));
        cloneItem.setItemMeta(meta);
        gui.setItem(24, cloneItem);

        ItemStack displayNameItem = new ItemStack(Material.NAME_TAG);
        meta = displayNameItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Edit item display name", "#AAAAAA"));
        displayNameItem.setItemMeta(meta);
        gui.setItem(29, displayNameItem);

        ItemStack pittyItem = new ItemStack(Material.TOTEM_OF_UNDYING);
        meta = pittyItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Pity system: " + (crate.pittySystemActive() ? "on" : "off"), "#00AAAA"));
        if(crate.pittySystemActive()) meta.setEnchantmentGlintOverride(true);
        pittyItem.setItemMeta(meta);
        gui.setItem(33, pittyItem);

        ItemStack crateItem;

        if(crateItemSelection) {
            crateItem = new ItemStack(Material.SCAFFOLDING);
            meta = crateItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click on new skull item or click here to abort", "#AA00AA").decoration(TextDecoration.BOLD, true));
            meta.setEnchantmentGlintOverride(true);
            crateItem.setItemMeta(meta);
        }
        else {
            crateItem = crate.getRenderItem().clone();
            meta = crateItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click to configure crate item", "#AA00AA").decoration(TextDecoration.BOLD, true));
            crateItem.setItemMeta(meta);
        }
        gui.setItem(20, crateItem);
    }

    public void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot < 54) {
                Component cancelComponent = Component.text(" [Cancel]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/crates cancel"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to Cancel", NamedTextColor.RED)))
                        .decorate(TextDecoration.BOLD);

                switch (slot) {
                    case 4:
                        Bukkit.dispatchCommand(sender, "crates give " + sender.getName() + " " + crate.getName() + " 64");
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new PreventCloseGui();
                    case 20:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new CrateEditGui(crate, !crateItemSelection, sender, plugin).getGui();
                    case 22:
                        sender.sendMessage(messageConfig.getColored("crate.info.type-url").append(cancelComponent));

                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            crate.setCrateSkullUrl(msg);
                            CrateStorage.saveCrate(crate);
                            sender.sendMessage(messageConfig.getColored("crate.success.value-updated"));

                            if (sender instanceof Player p) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    p.openInventory(
                                            new CrateEditGui(crate, false, sender, plugin)
                                                    .getGui().getInventory()
                                    );
                                });
                            }
                        });

                        if(sender instanceof Player p) p.closeInventory();
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;
                    case 24:
                        sender.sendMessage(messageConfig.getColored("crate.info.type-name").append(cancelComponent));

                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            if (sender instanceof Player p) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Bukkit.dispatchCommand(sender, "crates clone " + crate.getName() + " " + msg);
                                    p.openInventory(new CrateListGui(sender, plugin).getGui().getInventory());
                                });
                            }
                        });


                        if(sender instanceof Player p) p.closeInventory();
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;
                    case 29:
                        sender.sendMessage(messageConfig.getColored("crate.info.type-display-name").append(cancelComponent));

                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            if (sender instanceof Player p) {

                                ItemStack crateItem = crate.getCrateItem();
                                ItemMeta meta = crateItem.getItemMeta();
                                meta.displayName(MiniMessage.miniMessage().deserialize(msg));
                                crateItem.setItemMeta(meta);
                                crate.setCrateItem(crateItem);

                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    p.openInventory(new CrateEditGui(crate, false, sender, plugin).getGui().getInventory());
                                });
                            }
                        });

                        if(sender instanceof Player p) p.closeInventory();
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;
                    case 31:
                        Bukkit.dispatchCommand(sender, "crates rewards " + crate.getName());
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new PreventCloseGui();
                    case 33:
                        crate.setPittySystem(!crate.pittySystemActive());
                        CrateStorage.saveCrate(crate);
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new CrateEditGui(crate, false, sender, plugin).getGui();
                    case 46:
                        Bukkit.dispatchCommand(sender, "crates list");
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new PreventCloseGui();
                    case 53:
                        ItemStack deleteItem = new ItemStack(Material.LAVA_BUCKET);
                        ItemMeta meta = deleteItem.getItemMeta();
                        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Delete Crate", "#FF5555"));
                        deleteItem.setItemMeta(meta);

                        ItemStack backItem = new ItemStack(Material.ARROW);
                        meta = backItem.getItemMeta();
                        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Abort", "#AAAAAA"));
                        backItem.setItemMeta(meta);

                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                        return new ConfirmationGui("Delete Crate?", deleteItem, backItem, () -> {
                            Bukkit.dispatchCommand(sender, "crates delete " + crate.getName());
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                            return new CrateListGui(sender, plugin).getGui();
                        }, () -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return self;
                        }).getGui();

                    default:
                        return new PreventCloseGui();
                }
            }

            if(slot >= 54 && crateItemSelection) {
                if(item.getType().isAir()) {
                    return new PreventCloseGui();
                }

                if(item.getType() !=  Material.PLAYER_HEAD) {
                    ChatMessageConfig messageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();
                    sender.sendMessage(messageConfig.getColored("crate.error.non-skull-item-selected"));
                    if(sender instanceof Player p) McGuiFramework.getGuiSounds().playFailSound(p);
                    return new PreventCloseGui();
                }

                crate.setCrateItem(item);
                CrateStorage.saveCrate(crate);
                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                return new CrateEditGui(crate, false, sender, plugin).getGui();
            }

            return new PreventCloseGui();
        });
    }

    public InteractableGui getGui() {
        return gui;
    }
}
