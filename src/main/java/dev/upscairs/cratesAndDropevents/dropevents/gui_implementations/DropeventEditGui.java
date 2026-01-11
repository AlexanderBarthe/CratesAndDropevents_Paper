package dev.upscairs.cratesAndDropevents.dropevents.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateEditGui;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateListGui;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.helper.ConfirmationGui;
import dev.upscairs.cratesAndDropevents.resc.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.resc.DropeventStorage;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

import java.util.List;

public class DropeventEditGui {

    private Dropevent dropevent;
    private CommandSender sender;
    private Plugin plugin;
    private boolean renderItemSelection;
    private ChatMessageConfig messageConfig;

    private InteractableGui gui;

    public DropeventEditGui(Dropevent dropevent, boolean renderItemSelection, CommandSender sender, Plugin plugin) {
        gui = new InteractableGui(new ItemDisplayGui());
        configureClickReaction();

        this.dropevent = dropevent;
        this.sender = sender;
        this.plugin = plugin;
        this.renderItemSelection = renderItemSelection;
        messageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();

        gui.setTitle("Edit " + dropevent.getName());
        gui.setSize(54);

        placeItems();
    }

    public void placeItems() {

        ItemMeta meta;

        ItemStack backItem = new ItemStack(Material.ARROW);
        meta = backItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("To the overview", "#AAAAAA").decoration(TextDecoration.BOLD, true));
        backItem.setItemMeta(meta);
        gui.setItem(45, backItem);

        ItemStack rangeItem = new ItemStack(Material.COMPASS);
        meta = rangeItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Range: " + dropevent.getDropRange(), "#55FFFF").decoration(TextDecoration.BOLD, true));
        rangeItem.setItemMeta(meta);
        gui.setItem(10, rangeItem);

        ItemStack timeItem = new ItemStack(Material.CLOCK);
        meta = timeItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Duration: " + dropevent.getEventTimeSec() + "s", "#55FF55").decoration(TextDecoration.BOLD, true));
        timeItem.setItemMeta(meta);
        gui.setItem(12, timeItem);

        ItemStack editFolderItem = new ItemStack(Material.ENDER_CHEST);
        meta = editFolderItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Edit folder", "#00AAAA"));
        editFolderItem.setItemMeta(meta);
        gui.setItem(8, editFolderItem);


        ItemStack broadcastItem = new ItemStack(Material.BELL);
        meta = broadcastItem.getItemMeta();

        TextComponent broadcastBooleanComponent = dropevent.isBroadcasting() ?
                InvGuiUtils.generateDefaultTextComponent("on", "#55FF55")
                : InvGuiUtils.generateDefaultTextComponent("off", "#FF5555");

        meta.displayName(InvGuiUtils
                .generateDefaultTextComponent("Broadcasting: ", "#CCCCCC")
                .append(broadcastBooleanComponent)
                .decoration(TextDecoration.BOLD, true));
        meta.setEnchantmentGlintOverride(dropevent.isBroadcasting());
        broadcastItem.setItemMeta(meta);
        gui.setItem(19, broadcastItem);

        ItemStack minPlayerItem = new ItemStack(Material.PLAYER_HEAD);
        meta = minPlayerItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Min players to start: " + dropevent.getMinPlayers(), "#AA0000").decoration(TextDecoration.BOLD, true));
        minPlayerItem.setItemMeta(meta);
        gui.setItem(30, minPlayerItem);

        ItemStack lootItem = new ItemStack(Material.CHEST);
        meta = lootItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Configure Loot Pool", "#FFAA00").decoration(TextDecoration.BOLD, true));
        lootItem.setItemMeta(meta);
        gui.setItem(32, lootItem);

        ItemStack droppedItem = new ItemStack(Material.HOPPER);
        meta = droppedItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Dropped Items: " + dropevent.getDropCount(), "#AA00AA").decoration(TextDecoration.BOLD, true));
        droppedItem.setItemMeta(meta);
        gui.setItem(14, droppedItem);

        ItemStack countdownItem = new ItemStack(Material.SPYGLASS);
        meta = countdownItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultTextComponent("Countdown: " + dropevent.getCountdownSec() + "s", "#AA0000").decoration(TextDecoration.BOLD, true));
        countdownItem.setItemMeta(meta);
        gui.setItem(16, countdownItem);

        ItemStack startItem = new ItemStack(Material.FIREWORK_ROCKET);
        meta = startItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Start event here", "#00AA00"));
        startItem.setItemMeta(meta);
        gui.setItem(47, startItem);

        ItemStack startInstantlyItem = new ItemStack(Material.DRAGON_HEAD);
        meta = startInstantlyItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Start event here now", "#55FFFF"));
        startInstantlyItem.setItemMeta(meta);
        gui.setItem(48, startInstantlyItem);

        ItemStack removeItem = new ItemStack(Material.LAVA_BUCKET);
        meta = removeItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Delete Dropevent", "#FF5555"));
        removeItem.setItemMeta(meta);
        gui.setItem(53, removeItem);

        ItemStack cloneItem = new ItemStack(Material.EMERALD);
        meta = cloneItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Clone Event", "#55FF55"));
        cloneItem.setItemMeta(meta);
        gui.setItem(51, cloneItem);

        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        meta = teleportItem.getItemMeta();
        TextComponent teleportBooleanComponent = dropevent.isTeleportable() ?
                InvGuiUtils.generateDefaultTextComponent("yes", "#55FF55")
                : InvGuiUtils.generateDefaultTextComponent("no", "#FF5555");

        meta.displayName(InvGuiUtils
                .generateDefaultTextComponent("Teleportable: ", "#009999")
                .append(teleportBooleanComponent)
                .decoration(TextDecoration.BOLD, true));
        meta.setEnchantmentGlintOverride(dropevent.isTeleportable());
        teleportItem.setItemMeta(meta);
        gui.setItem(21, teleportItem);

        ItemStack renderItem;

        if(renderItemSelection) {
            renderItem = new ItemStack(Material.SCAFFOLDING);
            meta = renderItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click on new item or click here to abort", "#AA00AA").decoration(TextDecoration.BOLD, true));
            meta.setEnchantmentGlintOverride(true);
            renderItem.setItemMeta(meta);
        }
        else {
            renderItem = dropevent.getRenderItem().clone();
            meta = renderItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultTextComponent("Click to configure render item", "#AA00AA").decoration(TextDecoration.BOLD, true));
            renderItem.setItemMeta(meta);
        }
        gui.setItem(23, renderItem);

        ItemStack commandItem = new ItemStack(Material.COMMAND_BLOCK);
        meta = commandItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Configure command on startup", "#FFAA00"));
        String commandRun = dropevent.getStartupCommand() != null ? "/" + dropevent.getStartupCommand() : "None";
        meta.lore(List.of(InvGuiUtils.generateDefaultTextComponent(commandRun, "#AAAAAA")));
        commandItem.setItemMeta(meta);
        gui.setItem(25, commandItem);

        gui.placeItems();
    }

    private void configureClickReaction() {
        gui.onClick((slot, item, self) -> {

            Component cancelComponent = Component.text(" [Cancel]", NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand("/crates cancel"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to Cancel", NamedTextColor.RED)))
                    .decorate(TextDecoration.BOLD);

            if(slot < 54) {
                switch (slot) {
                    case 8:
                        sender.sendMessage(messageConfig.getColored("dropevent.info.type-folder").append(cancelComponent));
                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            if(sender instanceof Player p) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Bukkit.dispatchCommand(sender, "de move " + dropevent.getName() + " /" + msg);
                                    p.openInventory(new DropeventEditGui(dropevent, false, sender, plugin).getGui().getInventory());
                                });
                            }
                        });

                        if (sender instanceof Player p) p.closeInventory();
                        if (sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;

                    case 10:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new EditDropeventNumberGui(dropevent.getDropRange(), 0, 999, dropevent, "Range", sender).getGui();
                    case 12:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new EditDropeventNumberGui(dropevent.getEventTimeSec(), 1, 999, dropevent, "Duration", sender).getGui();
                    case 14:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new EditDropeventNumberGui(dropevent.getDropCount(), 1, 2500, dropevent, "Drops", sender).getGui();
                    case 16:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new EditDropeventNumberGui(dropevent.getCountdownSec(), 0, 999, dropevent, "Countdown", sender).getGui();
                    case 30:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new EditDropeventNumberGui(dropevent.getMinPlayers(), 0, 2500, dropevent, "Minplayers", sender).getGui();
                    case 32:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new DropeventDropsGui(dropevent, sender, plugin).getGui();
                    case 45:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new DropeventListGui(dropevent.getFolder(), sender, plugin).getGui();
                    case 47:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        Bukkit.dispatchCommand(sender, "dropevent start " + dropevent.getName());
                        return null;
                    case 48:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        Bukkit.dispatchCommand(sender, "dropevent start-now " + dropevent.getName());
                        return null;
                    case 23:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new DropeventEditGui(dropevent, !renderItemSelection, sender, plugin).getGui();
                    case 25:
                        sender.sendMessage(messageConfig.getColored("dropevent.info.type-command").append(cancelComponent));

                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            if(msg.isBlank() || msg.equalsIgnoreCase(".")) dropevent.setStartupCommand(null);
                            else dropevent.setStartupCommand(msg);
                            DropeventStorage.saveDropevent(dropevent);

                            if(sender instanceof Player p) {
                                McGuiFramework.getGuiSounds().playSuccessSound(p);
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    p.openInventory(new DropeventEditGui(dropevent, false, sender, plugin).getGui().getInventory());
                                });
                            }
                        });

                        if(sender instanceof Player p) p.closeInventory();
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;

                    case 19:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        dropevent.setBroadcasting(!dropevent.isBroadcasting());
                        DropeventStorage.saveDropevent(dropevent);
                        return new DropeventEditGui(dropevent, renderItemSelection, sender, plugin).getGui();
                    case 21:
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        dropevent.setTeleportable(!dropevent.isTeleportable());
                        DropeventStorage.saveDropevent(dropevent);
                        return new DropeventEditGui(dropevent, renderItemSelection, sender, plugin).getGui();
                    case 53:
                        ItemStack deleteItem = new ItemStack(Material.LAVA_BUCKET);
                        ItemMeta meta = deleteItem.getItemMeta();
                        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Delete Dropevent", "#FF5555"));
                        deleteItem.setItemMeta(meta);

                        ItemStack backItem = new ItemStack(Material.ARROW);
                        meta = backItem.getItemMeta();
                        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Abort", "#AAAAAA"));
                        backItem.setItemMeta(meta);

                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                        return new ConfirmationGui("Delete Dropevent?", deleteItem, backItem, () -> {
                            DropeventStorage.removeDropevent(dropevent);
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                            return new DropeventListGui(dropevent.getFolder(), sender, plugin).getGui();
                        }, () -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return self;
                        }).getGui();
                    case 51:
                        sender.sendMessage(messageConfig.getColored("dropevent.info.type-name").append(cancelComponent));

                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            if (sender instanceof Player p) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Bukkit.dispatchCommand(sender, "dropevent clone " + dropevent.getName() + " " + msg);
                                    p.openInventory(new DropeventListGui(dropevent.getFolder(), sender, plugin).getGui().getInventory());
                                });
                            }
                        });

                        if(sender instanceof Player p) p.closeInventory();
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;

                    default:
                        return new PreventCloseGui();
                }
            }
            if(slot >= 54 && renderItemSelection) {

                if(item.getType().isAir()) {
                    return new PreventCloseGui();
                }

                dropevent.setRenderItem(item);
                DropeventStorage.saveDropevent(dropevent);

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                return new DropeventEditGui(dropevent, false, sender, plugin).getGui();
            }


            return new PreventCloseGui();
        });
    }

    public InteractableGui getGui() {
        return gui;
    }




}
