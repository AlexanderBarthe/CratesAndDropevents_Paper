package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DEMoveSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final DropeventService dropeventService;

    public DEMoveSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.dropeventService = plugin.getDbServices().getDropeventService();
    }


    @Override
    public String name() {
        return "move";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.missing-id"));
            return true;
        }

        String folder = "";
        if(args.length >= 3) {
            folder = args[2].startsWith("/") ? args[2] : "/" + args[2];
            folder = folder.endsWith("/") ? folder.substring(0, folder.length() - 1) : folder;
        }

        if(folder.equals(".") || folder.equals("/.")) folder = "";

        Dropevent dropevent = dropeventService.getById(args[1]);

        if(dropevent == null) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.invalid-id"));
            return true;
        }

        dropevent.setFolder(folder);
        dropeventService.update(dropevent);

        sender.sendMessage(messageConfig.getColored("dropevent.success.setting-changed"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit") && sender instanceof Player;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
