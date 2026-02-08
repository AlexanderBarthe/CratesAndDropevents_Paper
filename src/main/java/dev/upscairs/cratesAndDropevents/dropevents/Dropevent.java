package dev.upscairs.cratesAndDropevents.dropevents;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.helper.FolderizableElement;
import dev.upscairs.cratesAndDropevents.helper.Serializer;
import dev.upscairs.mcGuiFramework.utility.InvGuiUtils;
import dev.upscairs.mcGuiFramework.utility.ListableGuiObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dropevent extends FolderizableElement implements ListableGuiObject {

    private int id;
    private ItemStack item;

    private int dropRange;
    private int eventTimeSec;
    private int dropCount;
    private int countdownSec;
    private boolean broadcast;
    private boolean teleportable;
    private String startupCommand;
    private int minPlayers;

    public static final NamespacedKey EVENT_KEY = new NamespacedKey(CratesAndDropevents.getInstance(),"DROPEVENT_ITEM");

    public Dropevent(String name, String folder) {
        super(folder);
        item = new ItemStack(Material.FIREWORK_ROCKET, 1);
        setName(InvGuiUtils.generateDefaultTextComponent(name, "#FFAA00"));
        dropRange = 100;
        eventTimeSec = 120;
        dropCount = 120;
        countdownSec = 120;
        broadcast = true;
        minPlayers = 0;
    }

    public Dropevent(String folder, ItemStack representingItem, int dropRange, int eventTimeSec, int dropCount, int countdownSec, boolean broadcast) {
        super(folder);
        this.item = representingItem;
        this.dropRange = dropRange;
        this.eventTimeSec = eventTimeSec;
        this.dropCount = dropCount;
        this.countdownSec = countdownSec;
        this.broadcast = broadcast;
        minPlayers = 0;
    }

    public Dropevent(int id, String folder, ItemStack item, int dropRange, int eventTimeSec, int dropCount, int countdownSec, boolean broadcast,  boolean teleportable, String startupCommand, int minPlayers) {
        super(folder);
        this.id = id;
        this.item = item;
        this.dropRange  = dropRange;
        this.eventTimeSec  = eventTimeSec;
        this.dropCount  = dropCount;
        this.countdownSec  = countdownSec;
        this.broadcast = broadcast;
        this.teleportable = teleportable;
        this.startupCommand = startupCommand;
        this.minPlayers = minPlayers;
    }

    public ItemStack getRenderItem() {
        ItemStack item = this.item.clone();
        ItemMeta meta = item.getItemMeta();

        List<Component> lore = meta.lore();
        if(lore == null) lore = new ArrayList<>();
        lore.add(InvGuiUtils.generateDefaultTextComponent("Id: " + id, "#555555"));
        meta.lore(lore);
        item.setItemMeta(meta);

        item.setAmount(1);

        return item;
    }

    public void setItem(ItemStack item) {
        ItemStack newItem = item.clone();

        //New item with old name

        newItem.setAmount(1);
        ItemMeta meta = newItem.getItemMeta();
        meta.displayName(getName());
        newItem.setItemMeta(meta);

        this.item = newItem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDropRange() {
        return dropRange;
    }

    public void setDropRange(int dropRange) {
        this.dropRange = dropRange;
    }

    public int getEventTimeSec() {
        return eventTimeSec;
    }

    public void setEventTimeSec(int eventTimeSec) {
        this.eventTimeSec = eventTimeSec;
    }

    public int getDropCount() {
        return dropCount;
    }

    public void setDropCount(int dropCount) {
        this.dropCount = dropCount;
    }

    public int getCountdownSec() {
        return countdownSec;
    }

    public void setCountdownSec(int countdownSec) {
        this.countdownSec = countdownSec;
    }

    public boolean isBroadcasting() {
        return broadcast;
    }

    public void setBroadcasting(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isTeleportable() {
        return teleportable;
    }

    public void setTeleportable(boolean teleportable) {
        this.teleportable = teleportable;
    }

    public String getStartupCommand() {
        return startupCommand;
    }

    public void setStartupCommand(String startupCommand) {
        this.startupCommand = startupCommand;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public ItemStack getItem() {
        return item;
    }

    public Dropevent clone() {
        return new Dropevent(id, getFolder(), item.clone(), dropRange, eventTimeSec,
                dropCount, countdownSec, broadcast, teleportable, startupCommand, minPlayers);
    }

    /**
     *
     * Adjust a setting via key-value-pair. Useful for command inputs
     *
     * @param setting
     * @param value
     * @return
     */
    public boolean changeSetting(String setting, String value) {

        if(setting.equalsIgnoreCase("renderItem")) {
            item = new ItemStack(Material.getMaterial(value.toUpperCase()));
            return true;
        }

        int newValue;

        try {
            newValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }

        if(newValue < 0 || newValue > 20000) return false;

        return switch (setting.toLowerCase()) {
            case "range" -> {
                dropRange = newValue;
                yield true;
            }
            case "duration" -> {
                eventTimeSec = newValue;
                yield true;
            }
            case "drops" -> {
                dropCount = newValue;
                yield true;
            }
            case "countdown" -> {
                countdownSec = newValue;
                yield true;
            }
            case "minplayers" -> {
                minPlayers = newValue;
                yield true;
            }
            default -> false;
        };

    }

    public static Dropevent deserialize(Map<String, Object> map) {

        String folder = (String) map.get("folder");
        if(folder == null) folder = "";

        Dropevent event = new Dropevent((String) map.get("name"), folder);
        event.setItem((ItemStack) map.get("renderItem"));
        event.setDropRange((int) map.get("dropRange"));
        event.setEventTimeSec((int) map.get("eventTimeSec"));
        event.setDropCount((int) map.get("dropCount"));
        event.setCountdownSec((int) map.get("countdownSec"));
        event.startupCommand = (String) map.get("startupCommand");

        if (map.containsKey("broadcast")) {
            event.setBroadcasting((boolean) map.get("broadcast"));
        }
        if (map.containsKey("teleportable")) {
            event.setTeleportable((boolean) map.get("teleportable"));
        }

        if(map.containsKey("minPlayers")) event.setMinPlayers((int) map.get("minPlayers"));
        else event.setMinPlayers(0);

        return event;
    }

    public Component getName() {
        return item.getItemMeta().displayName();
    }

    public String getNameRaw() {
        if(getName() == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(getName());
    }

    public void setName(Component name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
    }

    public void setName(String componentString) {
        setName(Serializer.parseStringToComponent(componentString));
    }

}
