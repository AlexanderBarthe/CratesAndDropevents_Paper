package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateRewardsGui;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CrRewardsSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final CrateService crateService;
    private final CratesAndDropevents plugin;

    public CrRewardsSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.crateService = plugin.getDbServices().getCrateService();
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "rewards";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player p))  return true;

        if(args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("system.command.error.not-enough-arguments"));
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messageConfig.getColored("crate.error.invalid-id"));
            return true;
        }

        Crate crate = crateService.getCrateById(id);

        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.invalid-id"));
            return true;
        }

        CrateRewardsGui cratesDropsGui = new CrateRewardsGui(crate, sender, plugin);
        p.openInventory(cratesDropsGui.getGui().getInventory());
        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.edit") && (sender instanceof Player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return Collections.emptyList();
    }
}
