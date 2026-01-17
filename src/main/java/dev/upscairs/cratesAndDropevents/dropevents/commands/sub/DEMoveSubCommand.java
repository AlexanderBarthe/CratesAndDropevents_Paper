package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DEMoveSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public DEMoveSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
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
        if (!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if (args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.missing-name"));
            return true;
        }

        String folder = "";
        if(args.length >= 3) {
            folder = args[2].startsWith("/") ? args[2] : "/" + args[2];
            folder = folder.endsWith("/") ? folder.substring(0, folder.length() - 1) : folder;
        }

        if(folder.equals(".") || folder.equals("/.")) folder = "";

        Dropevent dropevent = DropeventStorage.getDropeventByName(args[1]);

        if(dropevent == null) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.name-not-found"));
            return true;
        }

        dropevent.setFolder(folder);
        DropeventStorage.saveDropevent(dropevent);

        sender.sendMessage(messageConfig.getColored("dropevent.success.setting-changed"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit") && sender instanceof Player;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(isSenderPermitted(sender)) {
            if(args.length == 2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if(args.length == 3) return CrateStorage.getCrateIds();
        }

        return Collections.emptyList();
    }
}
