package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrMoveSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrMoveSubCommand(CratesAndDropevents plugin) {
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
            sender.sendMessage(messageConfig.getColored("crate.error.missing-name"));
            return true;
        }

        String folder = "";
        if(args.length >= 3) {
            folder = args[2].startsWith("/") ? args[2] : "/" + args[2];
            folder = folder.endsWith("/") ? folder.substring(0, folder.length() - 1) : folder;
        }

        if(folder.equals(".") || folder.equals("/.")) folder = "";

        Crate crate = CrateStorage.getCrateById(args[1]);

        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.name-not-found"));
            return true;
        }

        crate.setFolder(folder);
        CrateStorage.saveCrate(crate);

        sender.sendMessage(messageConfig.getColored("crate.success.value-updated"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.edit") && sender instanceof Player;
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
