package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CrDeleteSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrDeleteSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if(args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("crate.error.missing-name"));
            return true;
        }

        Crate crate = CrateStorage.getCrateById(args[1]);

        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.name-not-found"));
            return true;
        }

        CrateStorage.removeCrate(args[1]);
        sender.sendMessage(messageConfig.getColored("crate.success.deleted"));

        return true;

    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(isSenderPermitted(sender) && args.length == 2) return CrateStorage.getCrateIds();

        return Collections.emptyList();
    }
}
