package dev.upscairs.cratesAndDropevents.crates.management;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.CrateService;
import dev.upscairs.cratesAndDropevents.file_resources.CrateStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CratePlaceHandler implements Listener {

    private final CratesAndDropevents plugin;
    private final CrateService crateService;
    private final CrateOpener crateOpener;


    public CratePlaceHandler(CratesAndDropevents plugin) {
        this.plugin = plugin;
        this.crateService = plugin.getDbServices().getCrateService();
        this.crateOpener = new CrateOpener(plugin);
    }



    @EventHandler
    public void onCratePlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        ItemStack usedItem = event.getItemInHand();
        Location location = event.getBlock().getLocation().add(0.5, 0.5, 0.5);

        if (usedItem == null || !usedItem.hasItemMeta()) return;

        ItemMeta meta = usedItem.getItemMeta();

        if (meta == null) return;
        Integer crateIdObj = meta.getPersistentDataContainer().get(Crate.CRATE_KEY, PersistentDataType.INTEGER);
        if (crateIdObj == null) return;
        int crateId = crateIdObj;
        System.out.println(crateId);

        Crate crate = crateService.getCrateById(crateId);
        if(crate == null) return;

        event.setCancelled(true);

        ItemStack newItem = usedItem.clone();
        newItem.setAmount(usedItem.getAmount() - 1);

        if(newItem.getAmount() <= 0) {
            newItem = new ItemStack(Material.AIR);
        }

        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newItem);

        crateOpener.openCrate(crate, player, location);

    }



}
