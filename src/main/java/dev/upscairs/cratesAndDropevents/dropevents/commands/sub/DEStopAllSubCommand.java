package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.dropevents.management.DropEventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DEStopAllSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public DEStopAllSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "stopall";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        DropEventManager.stopAll();
        sender.sendMessage(messageConfig.getColored("dropevent.success.all-stopped"));
        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
