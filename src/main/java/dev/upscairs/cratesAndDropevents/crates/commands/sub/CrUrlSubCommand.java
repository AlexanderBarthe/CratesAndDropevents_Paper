package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.mcGuiFramework.McGuiFramework;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CrUrlSubCommand implements SubCommand {

    private final ChatMessageConfig messageConfig;
    private final CrateService crateService;

    public CrUrlSubCommand(CratesAndDropevents plugin) {
        this.messageConfig = plugin.getChatMessageConfig();
        this.crateService = plugin.getDbServices().getCrateService();
    }

    @Override
    public String name() {
        return "url";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!isSenderPermitted(sender)) return true;

        if(args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("system.command.error.missing-id"));
            return true;
        }
        else if(args.length == 2) {
            sender.sendMessage(messageConfig.getColored("crate.error.missing-url"));
            return true;
        }

        Crate crate = crateService.getCrateById(args[1]);

        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.invalid-id"));
            return true;
        }

        crate.setCrateSkullUrl(args[2]);
        crateService.updateCrate(crate);

        sender.sendMessage(messageConfig.getColored("crate.success.skull-updated"));
        if(sender instanceof Player p) McGuiFramework.getGuiSounds().playSuccessSound(p);

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
