package dev.upscairs.cratesAndDropevents.crates.management;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class CratePlaceHandler implements Listener {

    private Plugin plugin;

    public CratePlaceHandler(Plugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onCratePlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        ItemStack usedItem = event.getItemInHand();
        Location location = event.getBlock().getLocation().add(0.5, 0.5, 0.5);

        if (usedItem == null || !usedItem.hasItemMeta()) return;


        ItemMeta meta = usedItem.getItemMeta();
        String crateName = meta.getPersistentDataContainer().get(((CratesAndDropevents) plugin).CRATE_KEY, PersistentDataType.STRING);

        Crate crate = CrateStorage.getCrateById(crateName);

        if(crate == null) return;


        event.setCancelled(true);

        ItemStack newItem = usedItem.clone();
        newItem.setAmount(usedItem.getAmount() - 1);

        if(newItem.getAmount() <= 0) {
            newItem = new ItemStack(Material.AIR);
        }

        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newItem);

        CrateOpener.openCrate(crate, player, location);

    }



}
