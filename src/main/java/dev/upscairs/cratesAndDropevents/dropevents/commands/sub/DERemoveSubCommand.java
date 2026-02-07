package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class DERemoveSubCommand implements SubCommand {

    private final DropeventService dropeventService;
    private final ChatMessageConfig messageConfig;

    public DERemoveSubCommand(CratesAndDropevents plugin) {
        this.dropeventService = plugin.getDbServices().getDropeventService();
        this.messageConfig = plugin.getChatMessageConfig();
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

        if(args.length == 1) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.missing-id"));
            return true;
        }

        boolean success = dropeventService.deleteById(args[1]);

        if (success) sender.sendMessage(messageConfig.getColored("dropevent.success.removed"));
        else sender.sendMessage(messageConfig.getColored("dropevent.error.invalid-id"));

        return true;

    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
