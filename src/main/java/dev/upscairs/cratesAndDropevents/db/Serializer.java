package dev.upscairs.cratesAndDropevents.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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



}
