package dev.upscairs.cratesAndDropevents.dropevents.management;


import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.resc.DropeventStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DropeventItemHandler implements Listener {

    private final CratesAndDropevents plugin;

    public DropeventItemHandler(CratesAndDropevents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {

        ItemStack usedItem = event.getItem();
        Player player = event.getPlayer();

        if(usedItem != null && usedItem.getItemMeta().getPersistentDataContainer().has(Dropevent.EVENT_KEY)) {
            event.setCancelled(true);
            useEventIfPossible(usedItem, player);
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        ItemStack usedItem = event.getItemInHand();
        Player player = event.getPlayer();

        if(usedItem.getItemMeta().getPersistentDataContainer().has(Dropevent.EVENT_KEY)) {
            event.setCancelled(true);
            useEventIfPossible(usedItem, player);
        }
    }


    private void useEventIfPossible(ItemStack usedItem, Player player) {

        if(!player.isSneaking()) {
            player.sendMessage(plugin.getChatMessageConfig().getColored("dropevent.info.sneak-for-use"));
            return;
        }
        if(!plugin.getConfig().getBoolean("dropevents.normal-players.usable") && !player.isOp()) {
            player.sendMessage(plugin.getChatMessageConfig().getColored("dropevent.error.use-no-perm"));
            return;
        }

        String eventName = usedItem.getItemMeta().getPersistentDataContainer().get(Dropevent.EVENT_KEY, PersistentDataType.STRING);
        Dropevent dropevent = DropeventStorage.getDropeventByName(eventName);

        if(dropevent == null) {
            player.sendMessage(plugin.getChatMessageConfig().getColored("dropevent.error.use-no-perm"));
        }

        DropEventRunner der = new DropEventRunner(dropevent, player, plugin);
        boolean startSuccessful = der.startCountdown();

        if(startSuccessful) {
            ItemStack newItem = usedItem.clone();
            newItem.setAmount(usedItem.getAmount() - 1);

            if(newItem.getAmount() <= 0) {
                newItem = new ItemStack(Material.AIR);
            }

            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newItem);
        }

    }



}
