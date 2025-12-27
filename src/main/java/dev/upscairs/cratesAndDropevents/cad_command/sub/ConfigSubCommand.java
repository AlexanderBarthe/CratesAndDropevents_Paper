package dev.upscairs.cratesAndDropevents.cad_command.sub;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.helper.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigSubCommand implements SubCommand {

    private final CratesAndDropevents plugin;

    public ConfigSubCommand(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "config";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!isSenderPermitted(sender)) return true;

        Map<String, Object> config = filterLeafs(plugin.getConfig().getValues(true));

        if(args.length <= 1) {
            for(Map.Entry<String, Object> entry : config.entrySet()) {
                sender.sendMessage(entry.getKey() + " = " + entry.getValue());
            }
        }
        else if(args.length == 2) {
            if(config.containsKey(args[1])) {
                sender.sendMessage(args[1] + " = " + config.get(args[1]));
            }
            else {
                plugin.getChatMessageConfig().get("Unknown config");
            }
        }
        else if(args.length >= 3) {
            Object value = config.get(args[1]);

            boolean success = updateConfigValue(args[1], value, args[2]);

            if(success) {
                sender.sendMessage("Successfully updated config");
            }
            else {
                sender.sendMessage("Mismatched data types");
            }
        }

        return true;
    }

    @Override
    public boolean isSenderPermitted(CommandSender sender) {
        return sender.hasPermission("cad.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Map<String, Object> config = filterLeafs(plugin.getConfig().getValues(true));

        if (args.length <= 2) {
            return new ArrayList<>(config.keySet());
        }

        return List.of();
    }

    private Map<String, Object> filterLeafs(Map<String, Object> map) {

        Map<String, Object> result = new HashMap<>();

        map.forEach((key, value) -> {
            if (!value.toString().startsWith("MemorySection[path=")) {
                result.put(key, value);
            }
        });

        return result;
    }

    private boolean updateConfigValue(String key, Object value, String newValue) {


        if(value instanceof Boolean v) {
            if(newValue.equalsIgnoreCase("true")) {
                plugin.getConfig().set(key, true);
                plugin.saveConfig();
                return true;
            }
            else if(newValue.equalsIgnoreCase("false")) {
                plugin.getConfig().set(key, false);
                plugin.saveConfig();
                return true;
            }
            else {
                return false;
            }
        }
        else if(value instanceof Integer || value instanceof Long) {
            try {
                value = Long.parseLong(newValue);
                plugin.getConfig().set(key, value);
                plugin.saveConfig();
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        else if(value instanceof Double) {
            try {
                value = Double.parseDouble(newValue);
                plugin.getConfig().set(key, value);
                plugin.saveConfig();
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        else if(value instanceof String) {
            value = newValue;
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            return true;
        }
        else if(value instanceof List l) {

            if(l.contains(newValue)) l.remove(newValue);
            else l.add(newValue);

            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            return true;

        }

        return false;

    }
}
