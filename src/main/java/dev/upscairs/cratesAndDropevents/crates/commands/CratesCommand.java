package dev.upscairs.cratesAndDropevents.crates.commands;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.resc.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.crates.commands.sub.*;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class CratesCommand implements CommandExecutor, TabCompleter {

    private Plugin plugin;
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public CratesCommand(Plugin plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    public void registerCommands() {
        CratesAndDropevents p = (CratesAndDropevents) plugin;

        register(new CrCloneSubCommand(p));
        register(new CrCreateSubCommand(p));
        register(new CrUrlSubCommand(p));
        register(new CrGiveSubCommand(p));
        register(new CrInfoSubCommand(p));
        register(new CrListSubCommand(p));
        register(new CrRewardsSubCommand(p));
        register(new CrCancelSubCommand(p));
        register(new CrDeleteSubCommand(p));
        register(new CrLootSubCommand(p));
        register(new CrMoveSubCommand(p));
    }

    public void register(SubCommand cmd) {
        subcommands.put(cmd.name(), cmd);
        cmd.aliases().forEach(a -> subcommands.put(a, cmd));
    }

    @Override
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
