package dev.upscairs.cratesAndDropevents.cad_command.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VersionSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public VersionSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "version";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("Version: " + plugin.getPluginMeta().getVersion());
        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
