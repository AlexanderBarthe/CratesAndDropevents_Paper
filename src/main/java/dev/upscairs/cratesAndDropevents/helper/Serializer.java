package dev.upscairs.cratesAndDropevents.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class Serializer {

    private static final Gson GSON = new Gson();

    public static String itemStackToJson(ItemStack item) {
        Map<String, Object> map = item.serialize();
        return GSON.toJson(map);
    }

    public static ItemStack jsonToItemStack(String json) {
        Map<String, Object> map = GSON.fromJson(
                json,
                new TypeToken<Map<String, Object>>(){}.getType()
        );
        return ItemStack.deserialize(map);
    }

    public static Component parseStringToComponent(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        // Mini message codes
        if (input.contains("<")) {
            try {
                return MiniMessage.miniMessage().deserialize("<italic:false>" + input + "</italic>");
            } catch (Exception e) {
                return Component.text(input).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            }
        }

        // Legacy codes
        if (input.indexOf('&') >= 0 || input.indexOf('§') >= 0) {
            Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(input);

            boolean usedLegacyItalic = input.toLowerCase().contains("&o") || input.toLowerCase().contains("§o");
            if (!usedLegacyItalic) {
                return legacy.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            } else {
                return legacy;
            }
        }

        // Default
        return Component.text(input).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .color(NamedTextColor.YELLOW);
    }

}
