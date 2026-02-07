package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.dropevents.gui_implementations.DropeventEditGui;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DEInfoSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final DropeventService dropeventService;
    private final CratesAndDropevents plugin;

    public DEInfoSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.dropeventService = plugin.getDbServices().getDropeventService();
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player p)) return true;

        if(args.length == 1) {
            p.sendMessage(messageConfig.getColored("dropevent.error.missing-id"));
            return true;
        }

        Dropevent event = dropeventService.getById(args[1]);

        if (event == null) {
            p.sendMessage(messageConfig.getColored("dropevent.error.invalid-id"));
            return true;
        }

        DropeventEditGui editGui = new DropeventEditGui(event, false, sender, plugin);
        p.openInventory(editGui.getGui().getInventory());
        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit") && (sender instanceof Player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
