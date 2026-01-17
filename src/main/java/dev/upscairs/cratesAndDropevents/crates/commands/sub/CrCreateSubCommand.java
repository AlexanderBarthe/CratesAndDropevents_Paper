package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CrCreateSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrCreateSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }


    @Override
    public String name() {
        return "create";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if(args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("crate.error.missing-name"));
            return true;
        }

        if(CrateStorage.getCrateById(args[1]) != null) {
            sender.sendMessage(messageConfig.getColored("crate.error.already-exists"));
            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playFailSound(p);
            return true;
        }

        String folder = "";
        if(args.length >= 3) folder = args[2];

        Crate crate = new Crate(args[1], folder);
        CrateStorage.saveCrate(crate);

        sender.sendMessage(messageConfig.getColored("crate.success.created"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
