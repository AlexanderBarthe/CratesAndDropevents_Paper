package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class DERemoveSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public DERemoveSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if(args.length == 1) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.missing-name"));
            return true;
        }

        Dropevent dropevent = DropeventStorage.getDropeventByName(args[1]);

        if (dropevent == null) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.name-not-found"));
            return true;
        }

        DropeventStorage.removeDropevent(dropevent);
        sender.sendMessage(messageConfig.getColored("dropevent.success.removed"));

        return true;

    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(isSenderPermitted(sender) && args.length == 2) return DropeventStorage.getDropeventNames();
        return Collections.emptyList();
    }
}
