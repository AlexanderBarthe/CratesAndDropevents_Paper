package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

import com.google.gson.JsonObject;
import dev.upscairs.cratesAndDropevents.helper.EditMode;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public interface CrateRewardEvent extends ListableGuiObject {

    CompletableFuture<Void> execute(Player player, Location location);

    EditMode getAssociatedEditMode();

    CrateRewardEvent clone();

    JsonObject toJson();

}
