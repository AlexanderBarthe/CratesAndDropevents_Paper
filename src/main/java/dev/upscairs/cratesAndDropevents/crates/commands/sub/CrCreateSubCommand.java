package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CrCreateSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final CrateService crateService;

    public CrCreateSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.crateService = plugin.getDbServices().getCrateService();
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

        String name = "New crate";
        if(args.length > 1) name = args[1];

        String folder = "";
        if(args.length > 2) folder = args[2];

        Crate crate = new Crate(name, folder);

        crateService.createCrate(crate);

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
