package dev.upscairs.cratesAndDropevents.cad_command;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.cad_command.sub.CancelSubCommand;
import dev.upscairs.cratesAndDropevents.cad_command.sub.ConfigSubCommand;
import dev.upscairs.cratesAndDropevents.cad_command.sub.ReloadSubCommand;
import dev.upscairs.cratesAndDropevents.cad_command.sub.VersionSubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class CaDCommand implements CommandExecutor, TabCompleter {

    private Plugin plugin;
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public CaDCommand(Plugin plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    public void registerCommands() {

        CratesAndDropevents p = (CratesAndDropevents)  plugin;

        register(new CancelSubCommand(p));
        register(new ReloadSubCommand(p));
        register(new VersionSubCommand(p));
        register(new ConfigSubCommand(p));

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

        if(!sender.hasPermission("cad.admin")) return Arrays.asList();

        if(args.length == 1) {
            return Arrays.asList("reload", "version", "upgrade", "config");
        }

        SubCommand handler = subcommands.get(args[0]);
        if(handler == null) return Collections.emptyList();

        return handler.onTabComplete(sender, command, alias, args);

    }



}
