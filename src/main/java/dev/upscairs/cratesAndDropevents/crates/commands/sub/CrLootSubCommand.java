package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateLootpoolGui;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrLootSubCommand implements SubCommand {

    private CratesAndDropevents plugin;

    public CrLootSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "loot";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();
        Player p = (Player) sender;

        ItemStack heldItem = p.getInventory().getItemInMainHand();


        if(heldItem.getType() != Material.PLAYER_HEAD) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }


        String crateName = heldItem.getItemMeta().getPersistentDataContainer().get(Crate.CRATE_KEY, PersistentDataType.STRING);
        if(crateName == null || crateName.isEmpty()) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }


        Crate crate = CrateStorage.getCrateById(crateName);
        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }

        CrateLootpoolGui gui = new CrateLootpoolGui(crate, sender, plugin);

        p.openInventory(gui.getGui().getInventory());
        return true;

    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return plugin.getConfig().getBoolean("crates.normal-players.view-lootpool") && (sender instanceof Player);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of();
    }
}
