package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DECloneSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final DropeventService dropeventService;

    public DECloneSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.dropeventService = plugin.getDbServices().getDropeventService();
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

        if(args.length < 2) {
            sender.sendMessage(messageConfig.getColored("system.command.error.missing-id"));
            return true;
        }


        Dropevent originalDropevent = dropeventService.getById(args[1]);

        if(originalDropevent == null) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.invalid-id"));
            return true;
        }

        Dropevent clonedDropevent = originalDropevent.clone();

        dropeventService.create(clonedDropevent);

        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
        sender.sendMessage(messageConfig.getColored("dropevent.success.cloned"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
