package dev.upscairs.cratesAndDropevents.dropevents.commands;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.dropevents.commands.sub.*;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class DropeventCommand implements CommandExecutor, TabCompleter {

    private Plugin plugin;
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public DropeventCommand(Plugin plugin) {
        this.plugin = plugin;
        registerCommands();
    }


    public void registerCommands() {

        CratesAndDropevents c = (CratesAndDropevents) plugin;

        register(new DECloneSubCommand(c));
        register(new DECreateSubCommand(c));
        register(new DEEditSubCommand(c));
        register(new DEGiveSubCommand(c));
        register(new DEInfoSubCommand(c));
        register(new DEListSubCommand(c));
        register(new DERemoveSubCommand(c));
        register(new DEStartSubCommand(c));
        register(new DEStartNowSubCommand(c));
        register(new DEStopAllSubCommand(c));
        register(new DETpSubCommand(c));
        register(new DEMoveSubCommand(c));
    }

    private void register(SubCommand cmd) {
        subcommands.put(cmd.name(), cmd);
        cmd.aliases().forEach(a -> subcommands.put(a, cmd));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0) {
            ChatMessageConfig messageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();
            sender.sendMessage(messageConfig.getColored("system.command.error.not-found"));
            return true;
        }

        ChatMessageConfig messageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();

        SubCommand handler = subcommands.get(args[0]);
        if(handler == null) {
            sender.sendMessage(messageConfig.getColored("system.command.error.not-found"));
            return true;
        }

        return handler.execute(sender, args);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(args.length == 1) {
            List<String> completions = new ArrayList<>();
            for(SubCommand cmd : subcommands.values()) {
                if(cmd.isSenderPermitted(sender)) {
                    completions.add(cmd.name());
                }
            }
            return completions;
        }

        SubCommand handler = subcommands.get(args[0]);
        if(handler == null) return Collections.emptyList();

        return handler.onTabComplete(sender, command, alias, args);

    }

}
