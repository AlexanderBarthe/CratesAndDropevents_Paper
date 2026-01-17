package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateRewardsGui;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CrRewardsSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrRewardsSubCommand(CratesAndDropevents plugin) {
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
        if(!isSenderPermitted(sender)) return true;
        if(!(sender instanceof Player p))  return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if(args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("system.command.error.not-enough-arguments"));
        }

        Crate crate = CrateStorage.getCrateById(args[1]);

        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.name-not-found"));
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

        if(isSenderPermitted(sender) && args.length == 2) return CrateStorage.getCrateIds();

        return Collections.emptyList();
    }
}
