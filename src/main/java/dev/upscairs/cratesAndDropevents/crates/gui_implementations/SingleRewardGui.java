package dev.upscairs.cratesAndDropevents.crates.gui_implementations;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.helper.ConfirmationGui;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.ChatMessageInputHandler;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.*;
import dev.upscairs.cratesAndDropevents.helper.GuiItemTemplate;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import dev.upscairs.mcGuiFramework.base.ItemDisplayGui;
import dev.upscairs.mcGuiFramework.functionality.PreventCloseGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.InteractableGui;
import dev.upscairs.mcGuiFramework.gui_wrappers.PageGui;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

import static dev.upscairs.cratesAndDropevents.helper.EditMode.*;

public class SingleRewardGui {

    private Crate crate;
    private CrateReward reward;
    private CrateRewardEvent selectedEvent;
    private CommandSender sender;
    private Plugin plugin;
    private ChatMessageConfig messageConfig;

    private EditMode editMode;

    private PageGui gui;

    public SingleRewardGui(Crate crate, CrateReward reward, CrateRewardEvent selectedEvent, EditMode editMode, CommandSender sender, Plugin plugin) {

        gui = new PageGui(new InteractableGui(new ItemDisplayGui()), reward.getSequence(), 0);

        this.crate = crate;
        this.reward = reward;
        this.selectedEvent = selectedEvent;
        this.editMode = editMode;
        this.sender = sender;
        this.plugin = plugin;
        this.messageConfig = ((CratesAndDropevents)plugin).getChatMessageConfig();

        gui.setSize(54);
        gui.setTitle("Configure reward");

        placeItems();
        configureClickReaction();
    }

    public void placeItems() {

        ItemMeta meta;

        ItemStack addEventItem = new ItemStack(Material.CHEST_MINECART);
        meta = addEventItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Add Event", "#FFAA00"));
        meta.setEnchantmentGlintOverride(editMode == ADD_EVENT);
        if(editMode == ADD_EVENT) meta.lore(List.of(InvGuiUtils.generateDefaultTextComponent("Click again to cancel", "#FF5555")));
        addEventItem.setItemMeta(meta);

        ItemStack cloneRewardItem = GuiItemTemplate.CLONE.create("Clone Reward");

        ItemStack editChanceItem = new ItemStack(Material.CHEST);
        meta = editChanceItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Probability: " + ((float)crate.getRewards().get(reward))/10 + "%" , "#FFAA00"));
        meta.lore(List.of(InvGuiUtils.generateDefaultTextComponent("Click to configure", "#AA00AA")));
        editChanceItem.setItemMeta(meta);

        ItemStack simulateItem = new ItemStack(Material.REDSTONE_TORCH);
        meta = simulateItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Simulate Reward", "#55FFFF"));
        simulateItem.setItemMeta(meta);

        ItemStack deleteItem = new ItemStack((editMode == NONE) ? Material.LAVA_BUCKET : Material.MINECART);
        meta = deleteItem.getItemMeta();

        String deleteSubject = (editMode == NONE) ? "Reward" : "Event";
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Delete " + deleteSubject, "#FF5555"));

        deleteItem.setItemMeta(meta);

        //Items to add event

        ItemStack addDropItem = new ItemStack(Material.DIAMOND);
        meta = addDropItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Add drop", "#00AAAA"));
        addDropItem.setItemMeta(meta);

        ItemStack addCommandItem = new ItemStack(Material.COMMAND_BLOCK);
        meta = addCommandItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Add command", "#00AAAA"));
        addCommandItem.setItemMeta(meta);

        ItemStack addSoundItem = new ItemStack(Material.NOTE_BLOCK);
        meta = addSoundItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Add sound", "#00AAAA"));
        addSoundItem.setItemMeta(meta);

        ItemStack addMessageItem = new ItemStack(Material.PAPER);
        meta = addMessageItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Add message", "#00AAAA"));
        addMessageItem.setItemMeta(meta);

        ItemStack addDelayItem = new ItemStack(Material.CLOCK);
        meta = addDelayItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Add delay", "#00AAAA"));
        addDelayItem.setItemMeta(meta);

        //Items for editing event

        ItemStack shiftLeftItem = InvGuiUtils.generateCustomUrlHeadStack("http://textures.minecraft.net/texture/cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5");
        meta = shiftLeftItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Shift left", "#00AAAA"));
        shiftLeftItem.setItemMeta(meta);

        ItemStack shiftRightItem = InvGuiUtils.generateCustomUrlHeadStack("http://textures.minecraft.net/texture/956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311");
        meta = shiftRightItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Shift right", "#00AAAA"));
        shiftRightItem.setItemMeta(meta);


        ItemStack editDropItem = switch(editMode) {
            case EDIT_ITEM_EVENT_ITEM_SELECT -> new ItemStack(Material.SCAFFOLDING);
            case EDIT_ITEM_EVENT -> selectedEvent.getRenderItem().clone();
            default -> new ItemStack(Material.BEDROCK);
        };
        editDropItem.setAmount(1);
        meta = editDropItem.getItemMeta();
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent("Edit drop", "#00AAAA"));
        meta.setEnchantmentGlintOverride(editMode == EDIT_ITEM_EVENT_ITEM_SELECT);
        editDropItem.setItemMeta(meta);


        Material otherEventMat = switch (editMode) {
            case EDIT_COMMAND_EVENT -> Material.COMMAND_BLOCK;
            case EDIT_MESSAGE_EVENT -> Material.PAPER;
            case EDIT_DELAY_EVENT -> Material.CLOCK;
            case EDIT_SOUND_EVENT -> Material.NOTE_BLOCK;
            default -> Material.BEDROCK;
        };

        ItemStack editOtherEventItem = new ItemStack(otherEventMat);

        meta = editOtherEventItem.getItemMeta();

        String editOtherEventName = switch (editMode) {
            case EDIT_COMMAND_EVENT -> "Edit command";
            case EDIT_MESSAGE_EVENT -> "Edit message";
            case EDIT_DELAY_EVENT -> "Edit delay";
            case EDIT_SOUND_EVENT -> "Edit sound";
            default -> "";
        };
        meta.displayName(InvGuiUtils.generateDefaultHeaderComponent(editOtherEventName, "#00AAAA"));
        editOtherEventItem.setItemMeta(meta);



        switch (editMode) {
            case NONE -> {
                gui.setItem(46, GuiItemTemplate.BACK.create("To the overview"));
                gui.setItem(47, addEventItem);
                gui.setItem(48, cloneRewardItem);
                gui.setItem(49, editChanceItem);
                gui.setItem(50, simulateItem);
                gui.setItem(51, deleteItem);
            }
            case ADD_EVENT -> {
                gui.setItem(46, GuiItemTemplate.BACK.create("To the overview"));
                gui.setItem(47, addEventItem);

                gui.setItem(48, addDropItem);
                gui.setItem(49, addCommandItem);
                gui.setItem(50, addSoundItem);
                gui.setItem(51, addMessageItem);
                gui.setItem(52, addDelayItem);
            }
            case EDIT_ITEM_EVENT, EDIT_ITEM_EVENT_ITEM_SELECT -> {
                gui.setItem(46, GuiItemTemplate.BACK.create("To the overview"));
                gui.setItem(49, editDropItem);
                gui.setItem(51, deleteItem);
                gui.setItem(48, shiftLeftItem);
                gui.setItem(50, shiftRightItem);
            }
            case EDIT_SOUND_EVENT, EDIT_COMMAND_EVENT, EDIT_MESSAGE_EVENT, EDIT_DELAY_EVENT -> {
                gui.setItem(46, GuiItemTemplate.BACK.create("To the overview"));
                gui.setItem(49, editOtherEventItem);
                gui.setItem(51, deleteItem);
                gui.setItem(48, shiftLeftItem);
                gui.setItem(50, shiftRightItem);
            }
        }

        if(selectedEvent != null) {

            int selectedEventIndex = gui.getListedObjects().indexOf(selectedEvent);

            CrateRewardEvent selectedEventInList = (CrateRewardEvent) gui.getListedObjects().get(selectedEventIndex);
            ItemStack selectedRepresentingItem = selectedEventInList.getRenderItem();

            meta = selectedRepresentingItem.getItemMeta();
            meta.setEnchantmentGlintOverride(true);
            selectedRepresentingItem.setItemMeta(meta);

            if(selectedEventIndex >= gui.getPage()*45 && selectedEventIndex < (gui.getPage()+1)*45) {
                gui.setItem(selectedEventIndex%45, selectedRepresentingItem);
            }


        }

    }

    public void configureClickReaction() {
        gui.onClick((slot, item, self) -> {

            if(slot >= 0 && slot <= 44) {
                if(slot+45* getGui().getPage() >= reward.getSequence().size()) {
                    return new PreventCloseGui();
                }
                CrateRewardEvent clickedEvent = reward.getSequence().get(slot+45 * getGui().getPage());

                if(clickedEvent.equals(selectedEvent)) {
                    if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                    return new SingleRewardGui(crate, reward, null, NONE, sender, plugin).getGui();
                }

                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                EditMode nextEditMode = clickedEvent.getAssociatedEditMode();
                return new SingleRewardGui(crate, reward, clickedEvent, nextEditMode, sender, plugin).getGui();
            }

            if (editMode == NONE) {
                    if (slot == 46) {
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new CrateRewardsGui(crate, sender, plugin).getGui();
                    }
                    else if(slot == 47) {
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new SingleRewardGui(crate, reward, null, ADD_EVENT, sender, plugin).getGui();
                    }
                    else if(slot == 48) {
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                        CrateReward clonedReward = reward.clone();
                        int newChance = Math.min(crate.getUnusedChance(), crate.getRewards().get(reward));
                        crate.setRewardChance(clonedReward, newChance);

                        CrateStorage.saveCrate(crate);

                        return new CrateRewardsGui(crate, sender, plugin).getGui();
                    }
                    else if(slot == 49) {
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new CrateRewardChanceGui(crate.getRewards().get(reward), crate.getUnusedChance(), crate, reward, sender, plugin).getGui();
                    }
                    else if(slot == 50) {
                        if(sender instanceof Player player) reward.execute(player, getLookingAt(player, 2));
                        return new PreventCloseGui();
                    }
                    else if(slot == 51) {

                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);

                        return new ConfirmationGui("Delete Reward?",
                                GuiItemTemplate.DELETE.create("Delete Reward"),
                                GuiItemTemplate.BACK.create("Abort"),
                                () -> {
                            crate.removeReward(reward);
                            CrateStorage.saveCrate(crate);
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
                            return new CrateRewardsGui(crate, sender, plugin).getGui();
                        }, () -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return self;
                        }).getGui();

                    }

                }
            else if(editMode == ADD_EVENT) {

                    if(slot == 46) {
                        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                        return new CrateRewardsGui(crate, sender, plugin).getGui();
                    }
                    else if(slot >= 47 && slot <= 52) {

                        CrateRewardEvent rewardEvent = switch (slot) {
                            case 48 -> new ItemRewardEvent(new ItemStack(Material.DIAMOND));
                            case 49 -> new CommandRewardEvent("help", plugin);
                            case 50 -> new SoundRewardEvent("minecraft:entity.experience_orb.pickup", 1, 1);
                            case 51 -> new MessageRewardEvent("<blue>hey");
                            case 52 -> new DelayRewardEvent(20, plugin);
                            default -> null;
                        };

                        if(rewardEvent != null) reward.addEvent(rewardEvent);
                        CrateStorage.saveCrate(crate);



                        if(sender instanceof Player p) {
                            if(slot == 47) {
                                McGuiFramework.getGuiSounds().playClickSound(p);
                            }
                            else {
                                McGuiFramework.getGuiSounds().playSuccessSound(p);
                            }
                        }

                        return new SingleRewardGui(crate, reward, null, NONE, sender, plugin).getGui();
                    }
            }
            else {

                Component cancelComponent = Component.text(" [Cancel]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/crates cancel"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to Cancel", NamedTextColor.RED)))
                        .decorate(TextDecoration.BOLD);

                if(slot == 46) {
                    if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                    return new SingleRewardGui(crate, reward, null, NONE, sender, plugin).getGui();
                }
                else if(slot == 48) {
                    List<CrateRewardEvent> rewards = reward.getSequence();
                    int index = rewards.indexOf(selectedEvent);
                    if(index > 0) {
                        Collections.swap(rewards, index, index - 1);
                    }
                    CrateStorage.saveCrate(crate);

                    if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                    return new SingleRewardGui(crate, reward, selectedEvent, editMode, sender, plugin).getGui();
                }
                else if(slot == 50) {
                    List<CrateRewardEvent> rewards = reward.getSequence();
                    int index = rewards.indexOf(selectedEvent);
                    if(index < rewards.size() - 1) {
                        Collections.swap(rewards, index, index + 1);
                    }
                    CrateStorage.saveCrate(crate);

                    if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                    return new SingleRewardGui(crate, reward, selectedEvent, editMode, sender, plugin).getGui();
                }
                else if(slot == 51) {
                    List<CrateRewardEvent> rewards = reward.getSequence();
                    rewards.remove(selectedEvent);
                    CrateStorage.saveCrate(crate);
                    return new SingleRewardGui(crate, reward, null, NONE, sender, plugin).getGui();
                }
                else if(slot == 49) {
                    switch (editMode) {
                        case EDIT_ITEM_EVENT -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return new SingleRewardGui(crate, reward, selectedEvent, EDIT_ITEM_EVENT_ITEM_SELECT, sender, plugin).getGui();
                        }
                        case EDIT_ITEM_EVENT_ITEM_SELECT -> {
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return new SingleRewardGui(crate, reward, selectedEvent, EDIT_ITEM_EVENT, sender, plugin).getGui();
                        }
                        case EDIT_SOUND_EVENT -> {

                            sender.sendMessage(messageConfig.getColored("crate.info.type-sound").append(cancelComponent));

                            ChatMessageInputHandler.addListener(sender, (msg) -> {
                                String args[] = msg.split(" ");
                                if(selectedEvent instanceof SoundRewardEvent srw) {
                                    srw.setSound(args[0]);
                                    try {
                                        srw.setVolume(Float.parseFloat(args[1]));
                                        srw.setPitch(Float.parseFloat(args[2]));
                                    } catch (Exception ignored) {}

                                    CrateStorage.saveCrate(crate);
                                }
                                sender.sendMessage(messageConfig.getColored("crate.success.value-updated"));

                                if (sender instanceof Player p) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        p.openInventory(
                                                new SingleRewardGui(crate, reward, null, NONE, sender, plugin)
                                                        .getGui().getInventory()
                                        );
                                    });
                                }
                            });
                            if(sender instanceof Player p) p.closeInventory();
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return null;
                        }
                        case EDIT_COMMAND_EVENT -> {

                            sender.sendMessage(messageConfig.getColored("crate.info.type-command").append(cancelComponent));

                            ChatMessageInputHandler.addListener(sender, (msg) -> {
                                if(selectedEvent instanceof CommandRewardEvent crw) crw.setCommand(msg);
                                CrateStorage.saveCrate(crate);
                                sender.sendMessage(messageConfig.getColored("crate.success.value-updated"));

                                if (sender instanceof Player p) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        p.openInventory(
                                                new SingleRewardGui(crate, reward, null, NONE, sender, plugin)
                                                        .getGui().getInventory()
                                        );
                                    });
                                }

                            });
                            if(sender instanceof Player p) p.closeInventory();
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return null;
                        }
                        case EDIT_DELAY_EVENT -> {
                            if(selectedEvent instanceof DelayRewardEvent drw) {
                                if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                                return new CrateRewardDelaySelection(drw.getTicks(), reward, crate, drw, sender, plugin).getGui();
                            }
                            return new PreventCloseGui();
                        }
                        case EDIT_MESSAGE_EVENT -> {

                            sender.sendMessage(messageConfig.getColored("crate.info.type-message").append(cancelComponent));

                            ChatMessageInputHandler.addListener(sender, (msg) -> {
                                if(selectedEvent instanceof MessageRewardEvent mrw) mrw.setMessageRaw(msg);
                                CrateStorage.saveCrate(crate);
                                sender.sendMessage(messageConfig.getColored("crate.success.value-updated"));

                                if (sender instanceof Player p) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        p.openInventory(
                                                new SingleRewardGui(crate, reward, null, NONE, sender, plugin)
                                                        .getGui().getInventory()
                                        );
                                    });
                                }
                            });
                            if(sender instanceof Player p) p.closeInventory();
                            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                            return null;
                        }

                    }
                }
                else if(slot >= 54 && editMode == EDIT_ITEM_EVENT_ITEM_SELECT) {
                    if(selectedEvent instanceof ItemRewardEvent ire) {
                        ire.setItem(item);
                    }
                    CrateStorage.saveCrate(crate);

                    if(sender instanceof Player p) McGuiFramework.getGuiSounds().playClickSound(p);
                    return new SingleRewardGui(crate, reward, selectedEvent, EDIT_ITEM_EVENT, sender, plugin).getGui();
                }

            }


            return null;

        });

    }

    private static Location getLookingAt(Player player, double maxDist) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                eye,
                dir,
                maxDist,
                FluidCollisionMode.NEVER,
                true
        );

        if (result != null) {
            if (result.getHitBlock() != null) {
                return result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
            } else if (result.getHitPosition() != null) {
                return result.getHitPosition().toLocation(player.getWorld());
            }
        }

        return eye.add(dir.multiply(maxDist));
    }

    public PageGui getGui() {
        return gui;
    }
}
