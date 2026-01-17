package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DECloneSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public DECloneSubCommand(CratesAndDropevents plugin) {
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
            sender.sendMessage(messageConfig.getColored("dropevent.error.missing-name"));
            return true;
        }

        Dropevent originalDropevent = DropeventStorage.getDropeventByName(args[1]);

        if(originalDropevent == null) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.name-not-found"));
            return true;
        }

        if (DropeventStorage.getDropeventByName(args[2]) != null) {
            if(sender instanceof Player p) McGuiFramework.getGuiSounds().playFailSound(p);
            sender.sendMessage(messageConfig.getColored("dropevent.error.name-already-exists"));
            return true;
        }

        Dropevent clonedCrate = originalDropevent.clone();
        clonedCrate.setName(args[2]);

        DropeventStorage.saveDropevent(clonedCrate);

        sender.sendMessage(messageConfig.getColored("dropevent.success.cloned"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(isSenderPermitted(sender) && args.length == 2) return DropeventStorage.getDropeventNames();
        return Collections.emptyList();
    }
}
