package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.helper.ConfirmationGui;
import dev.upscairs.cratesAndDropevents.helper.GuiItemTemplate;
import dev.upscairs.mcGuiFramework.McGuiFramework;
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

        gui.setItem(8, GuiItemTemplate.FOLDER_CONFIG.create());

        gui.setItem(53, GuiItemTemplate.DELETE.create("Delete crate"));

        gui.setItem(46, GuiItemTemplate.BACK.create("To the overview"));

        gui.setItem(31, GuiItemTemplate.LOOTPOOL.create("Configure rewards"));

        ItemStack urlItem = new ItemStack(Material.WRITABLE_BOOK);
        meta = urlItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Click to edit Skull Url", "#FFAA00"));
        meta.lore(List.of(InvGuiUtils.generateDefaultTextComponent("Or use /crates url <crate-name> <url>", "#AA00AA")));
        urlItem.setItemMeta(meta);
        gui.setItem(22, urlItem);

        gui.setItem(24, GuiItemTemplate.CLONE.create("Clone crate"));

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
            meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Click on new skull item or click here to abort", "#AA00AA"));
            meta.setEnchantmentGlintOverride(true);
            crateItem.setItemMeta(meta);
        }
        else {
            crateItem = crate.getRenderItem().clone();
            meta = crateItem.getItemMeta();
            meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Click to configure crate item", "#AA00AA"));
            crateItem.setItemMeta(meta);
        }
        gui.setItem(20, crateItem);
    }

    public void configureClickReaction() {
        gui.onClick((slot, item, self) -> {
            if(slot < 54) {
                Component cancelComponent = Component.text(" [Cancel]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/cad cancel"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to Cancel", NamedTextColor.RED)))
                        .decorate(TextDecoration.BOLD);

                switch (slot) {
                    case 4:
                        Bukkit.dispatchCommand(sender, "crates give " + sender.getName() + " " + crate.getName() + " 64");
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new PreventCloseGui();
                    case 8:
                        sender.sendMessage(messageConfig.getColored("crate.info.type-folder").append(cancelComponent));

                        ChatMessageInputHandler.addListener(sender, (msg) -> {
                            if(sender instanceof Player p) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Bukkit.dispatchCommand(sender, "crates move " + crate.getName() + " /" + msg);
                                    p.openInventory(new CrateEditGui(crate, false, sender, plugin).getGui().getInventory());
                                });
                            }
                        });

                        if (sender instanceof Player p) p.closeInventory();
                        if (sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return null;
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
                                    p.openInventory(new CrateListGui(crate.getFolder(), sender, plugin).getGui().getInventory());
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
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new CrateListGui(crate.getFolder(), sender, plugin).getGui();
                    case 53:

                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                        return new ConfirmationGui("Delete Crate?",
                                GuiItemTemplate.DELETE.create("Delete Crate"),
                                GuiItemTemplate.BACK.create("Abort"),
                                () -> {
                            Bukkit.dispatchCommand(sender, "crates delete " + crate.getName());
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                            return new CrateListGui(crate.getFolder(), sender, plugin).getGui();
                        }, () -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return self;
                        }).getGui();

                    default:
                        return new PreventCloseGui();
                }
            }

            if(crateItemSelection) {
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
