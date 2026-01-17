package dev.upscairs.cratesAndDropevents.crates.commands.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrGiveSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public CrGiveSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }


    @Override
    public String name() {
        return "give";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!isSenderPermitted(sender)) return true;

        ChatMessageConfig messageConfig = plugin.getChatMessageConfig();

        if (args.length <= 1) {
            sender.sendMessage(messageConfig.getColored("crate.error.missing-player"));
            return true;
        }

        if(args.length == 2) {
            sender.sendMessage(messageConfig.getColored("crate.error.missing-name"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if(target == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.player-not-found"));
            return true;
        }

        Crate crate = CrateStorage.getCrateById(args[2]);

        if(crate == null) {
            sender.sendMessage(messageConfig.getColored("crate.error.name-not-found"));
            return true;
        }


        int amount = 1;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ignored) {}

        ItemStack item = crate.getCrateItem().clone();
        item.setAmount(amount);

        target.getInventory().addItem(item);

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.crates.give");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(isSenderPermitted(sender)) {
            if(args.length == 2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if(args.length == 3) return CrateStorage.getCrateIds();
        }

        return Collections.emptyList();
    }
}
