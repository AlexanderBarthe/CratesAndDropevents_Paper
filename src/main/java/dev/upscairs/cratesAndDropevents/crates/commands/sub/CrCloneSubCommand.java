package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CrCloneSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrCloneSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }


    @Override
    public String name() {
        return "clone";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if(args.length <= 2) {
            sender.sendMessage(messageConfig.getColored("crate.error.missing-name"));
            return true;
        }

        Crate originalCrate = CrateStorage.getCrateById(args[1]);

        if(originalCrate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.name-not-found"));
            return true;
        }

        if(CrateStorage.getCrateById(args[2]) != null) {
            sender.sendMessage(messageConfig.getColored("crate.error.already-exists"));
            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playFailSound(p);
            return true;
        }

        Crate clonedCrate = originalCrate.clone();
        clonedCrate.setName(args[2]);

        CrateStorage.saveCrate(clonedCrate);

        sender.sendMessage(messageConfig.getColored("crate.success.cloned"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(isSenderPermitted(sender) && args.length == 2) return CrateStorage.getCrateIds();

        return Collections.emptyList();

    }
}
