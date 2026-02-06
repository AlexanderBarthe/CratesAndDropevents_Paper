package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CrCloneSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final CrateService crateService;

    public CrCloneSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.crateService = plugin.getDbServices().getCrateService();
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

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messageConfig.getColored("crate.error.invalid-id"));
            return true;
        }

        Crate originalCrate = crateService.getCrateById(id);

        if(originalCrate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.invalid-id"));
            return true;
        }


        Crate clonedCrate = originalCrate.clone();

        //Optional: Set name of crate
        if(args.length >= 3) clonedCrate.setName(args[2]);

        crateService.createCrate(clonedCrate);

        sender.sendMessage(messageConfig.getColored("crate.success.cloned"));

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return Collections.emptyList();

    }
}
