package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.crates.gui_implementations.CrateEditGui;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CrInfoSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrInfoSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }


    @Override
    public String name() {
        return "info";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!isSenderPermitted(sender)) return true;
        if(!(sender instanceof Player p)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if(args.length <= 1) {
            p.sendMessage(messageConfig.getColored("dropevent.error.missing-name"));
            return true;
        }

        Crate crate = CrateStorage.getCrateById(args[1]);

        if (crate == null) {
            p.sendMessage(messageConfig.getColored("dropevent.error.name-not-found"));
            return true;
        }

        CrateEditGui editGui = new CrateEditGui(crate, false, sender, plugin);
        p.openInventory(editGui.getGui().getInventory());
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
