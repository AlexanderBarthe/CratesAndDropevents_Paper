package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateLootpoolGui;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrLootSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final CrateService crateService;
    private final CratesAndDropevents plugin;

    public CrLootSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.crateService = plugin.getDbServices().getCrateService();
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

        Player p = (Player) sender;

        ItemStack heldItem = p.getInventory().getItemInMainHand();


        if(heldItem.getType() != Material.PLAYER_HEAD) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }

        ItemMeta meta = heldItem.getItemMeta();

        if (meta == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }
        Integer crateIdObj = meta.getPersistentDataContainer().get(Crate.CRATE_KEY, PersistentDataType.INTEGER);
        if (crateIdObj == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }
        int crateId = crateIdObj;

        Crate crate = crateService.getCrateById(crateId);
        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.no-crate-in-hand"));
            return true;
        }

        CrateLootpoolGui gui = new CrateLootpoolGui(crate, plugin);

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
