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

import java.util.List;

public class DECreateSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final DropeventService dropeventService;

    public DECreateSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.dropeventService = plugin.getDbServices().getDropeventService();
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

        String eventName = "New Dropevent";
        String folder = "";

        if(args.length >= 2) eventName = args[1];
        if(args.length >= 3) folder = args[2];


        Dropevent dropevent = new Dropevent(eventName, folder);

        dropeventService.create(dropevent);

        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);
        sender.sendMessage(messageConfig.getColored("dropevent.success.created"));
        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
