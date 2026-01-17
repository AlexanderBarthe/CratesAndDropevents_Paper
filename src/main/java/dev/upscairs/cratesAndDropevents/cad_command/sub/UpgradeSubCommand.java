package dev.upscairs.cratesAndDropevents.cad_command.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.CommandRewardEvent;
import dev.upscairs.cratesAndDropevents.crates.rewards.payouts.CrateRewardEvent;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import dev.upscairs.cratesAndDropevents.file_resources.DropeventStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UpgradeSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public UpgradeSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "upgrade";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!isSenderPermitted(sender)) return true;

        List<Dropevent> dropevents = DropeventStorage.getAll();

        for (Dropevent dropevent : dropevents) {
            dropevent.setStartupCommand(replaceCodes(dropevent.getStartupCommand()));
            DropeventStorage.saveDropevent(dropevent);
        }

        List<Crate>  crates = CrateStorage.getAll();

        for (Crate crate : crates) {
            for(CrateReward reward : crate.getRewards().keySet()) {
                for(CrateRewardEvent cre : reward.getSequence()) {
                    if(!(cre instanceof CommandRewardEvent commandRewardEvent)) continue;
                    commandRewardEvent.setCommand(replaceCodes(commandRewardEvent.getCommand()));
                    CrateStorage.saveCrate(crate);
                }
            }
        }

        sender.sendMessage("Upgrade process finished!");
        return true;


    }

    private String replaceCodes(String string) {
        if(string == null) return null;
        return string
                .replace("{world}", "%w")
                .replace("{player}", "%p")
                .replace("{location}", "%l");
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
