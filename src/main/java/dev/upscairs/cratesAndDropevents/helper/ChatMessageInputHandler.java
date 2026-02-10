package dev.upscairs.cratesAndDropevents.helper;

import dev.upscairs.mcGuiFramework.McGuiFramework;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.function.Consumer;

public class ChatMessageInputHandler implements Listener {

    private static final HashMap<CommandSender, Consumer<String>> listeners = new HashMap<>();

    public ChatMessageInputHandler() {}

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        CommandSender sender = event.getPlayer();

        if(listeners.containsKey(sender)) {
            event.setCancelled(true);
            Component message = event.message();

            String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);

            listeners.get(sender).accept(plainMessage);

            removeListener(sender);

        }

    }

    public static void addListener(CommandSender sender, Consumer<String> consumer) {
        listeners.put(sender, consumer);
    }

    public static void removeListener(CommandSender sender) {
        listeners.remove(sender);
    }





}
