package dev.upscairs.cratesAndDropevents.dropevents.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DEEditSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final DropeventService dropeventService;

    public DEEditSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.dropeventService = plugin.getDbServices().getDropeventService();
    }


    @Override
    public String name() {
        return "edit";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(args.length < 3) {
            sender.sendMessage(messageConfig.getColored("system.command.error.not-enough-arguments"));
            return  true;
        }

        String id = args[1];
        String setting = args[2];
        String value = args[3];

        Dropevent event = dropeventService.getById(id);

        if (event == null) {
            sender.sendMessage(messageConfig.getColored("dropevent.error.invalid-id"));
            return true;
        }

        boolean success = event.changeSetting(setting, value);

        dropeventService.update(event);

        if (success) {
            sender.sendMessage(messageConfig.getColored("dropevent.success.setting-changed"));
        } else {
            sender.sendMessage(messageConfig.getColored("dropevent.error.setting-update-failed"));
        }

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.dropevents.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(!isSenderPermitted(sender)) return Collections.emptyList();

        else if(args.length == 3) {
            return Arrays.asList("renderItem", "range", "duration", "drops", "countdown", "minPlayers");
        }
        else if(args.length == 4 && args[2].equalsIgnoreCase("renderItem")) {
            String partial = args[3].toUpperCase();
            return Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(name -> name.startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }
}
