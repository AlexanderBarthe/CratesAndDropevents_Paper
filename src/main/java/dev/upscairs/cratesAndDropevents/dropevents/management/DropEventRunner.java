package dev.upscairs.cratesAndDropevents.dropevents.management;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.file_resources.ChatMessageConfig;
import dev.upscairs.cratesAndDropevents.helper.BossbarCountdown;
import dev.upscairs.cratesAndDropevents.helper.BroadcastService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DropEventRunner {

    private Dropevent dropevent;
    private Location centerLocation;
    private Player hostingPlayer;

    private String locationName = "";

    private Plugin plugin;
    private ChatMessageConfig chatMessageConfig;
    private Random random = new Random();

    private UUID eventId;
    private ActiveDropEvent activeDropEvent;

    private static final int COUNTDOWN_FIREWORK_INTERVAL = 8;

    public DropEventRunner(Dropevent dropevent, Player hostingPlayer, Plugin plugin) {
        this.dropevent = dropevent.clone();
        this.centerLocation = hostingPlayer.getLocation().clone();
        this.hostingPlayer = hostingPlayer;
        this.plugin = plugin;
        this.chatMessageConfig = ((CratesAndDropevents) plugin).getChatMessageConfig();
    }

    public boolean startInstantly() {
        dropevent.setCountdownSec(0);
        return startCountdown();
    }

    public boolean startInstantly(String locationName) {
        dropevent.setCountdownSec(0);
        return startCountdown(locationName);
    }


    public boolean startCountdown(String locationName) {
        this.locationName = locationName;
        return startCountdown();
    }

    /**
     *
     * Starts countdown for dropevent. Schedules start of dropevent after countdown.
     * Sends broadcast messages.
     *
     */
    public boolean startCountdown() {

        if(!eventStartable()) {
            return false;
        }

        eventId = DropEventManager.register(plugin, dropevent, centerLocation);
        activeDropEvent = DropEventManager.get(eventId);

        //Firework marking middle while countdown
        for(int i = 0; i < dropevent.getCountdownSec() / COUNTDOWN_FIREWORK_INTERVAL; i++) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                createRandomFirework(centerLocation, 1);
            }, i * COUNTDOWN_FIREWORK_INTERVAL * 20);
            activeDropEvent.addTask(task);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> startEvent(), dropevent.getCountdownSec()*20);
        activeDropEvent.addTask(task);

        //Spawn dragon 10 secs before dropevent start
        if(plugin.getConfig().getBoolean("dropevents.starter-dragon")) {
            BukkitTask dragonTask = Bukkit.getScheduler().runTaskLater(plugin, () -> spawnDyingDragon(),
                    dropevent.getCountdownSec() < 8 ? 0 : (dropevent.getCountdownSec() - 10) * 20);
            activeDropEvent.addTask(dragonTask);
        }

        //Send chat messages
        BroadcastService.sendLocalBroadcast(centerLocation, dropevent.getDropRange()+25,
                translateBroadDataCodes(chatMessageConfig.get("dropevent.broadcast.local.countdown")));

        if(dropevent.isBroadcasting()) {
            BroadcastService.sendGlobalBroadcast(translateBroadDataCodes(chatMessageConfig.get("dropevent.broadcast.global.countdown")));
        }

        //Create bossbar
        if(plugin.getConfig().getBoolean("dropevents.bossbar-countdown")) {
            BossbarCountdown bossbarCountdown = new BossbarCountdown(plugin, centerLocation, dropevent.getDropRange(), dropevent.getCountdownSec());
            bossbarCountdown.start();
            activeDropEvent.setBossbarCountdown(bossbarCountdown);
        }

        return true;

    }

    /**
     *
     * Starts the event. Sends broadcasts and schedules the drops of the event.
     *
     */
    private void startEvent() {

        List<Double> dropTimes = generateDropTimes();

        if(dropevent.getStartupCommand() != null && !dropevent.getStartupCommand().isEmpty() && !dropevent.getStartupCommand().equals(".")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolveStartupCommand());
        }

        BroadcastService.sendLocalBroadcast(centerLocation,
                dropevent.getDropRange()+25,
                translateBroadDataCodes(chatMessageConfig.get("dropevent.broadcast.local.start")));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BroadcastService.sendLocalBroadcast(centerLocation, dropevent.getDropRange()+25,
                    translateBroadDataCodes(chatMessageConfig.get("dropevent.broadcast.local.end")));
        }, dropevent.getEventTimeSec()*20);
        activeDropEvent.addTask(task);

        for (double dropTimeSec : dropTimes) {
            long delayTicks = (long) (dropTimeSec * 20);
            BukkitTask dropTask = Bukkit.getScheduler().runTaskLater(plugin, () -> executeDrop(), delayTicks);
            activeDropEvent.addTask(dropTask);
        }

    }

    private String resolveStartupCommand() {
        String ESCAPED_PERCENT = "__ESCAPED_PERCENT-YINm8eZh2z7mDF4oB7Cl__";

        String resolved = dropevent.getStartupCommand().replace("\\%", ESCAPED_PERCENT);

        resolved = resolved
                .replace("%p", hostingPlayer.getName())
                .replace("%w", centerLocation.getWorld().getKey().asString())
                .replace("%l", centerLocation.getX() + " " + centerLocation.getY() + " " + centerLocation.getZ());

        resolved = resolved.replace(ESCAPED_PERCENT, "%");

        return resolved;
    }

    private boolean eventStartable() {

        FileConfiguration config = plugin.getConfig();

        if(config.getBoolean("dropevents.ops-override-restrictions") && hostingPlayer.isOp()) {
            return true;
        }


        if(config.getBoolean("dropevents.simultaneous-limit.active")
            && DropEventManager.getActiveCount() >= config.getInt("dropevents.simultaneous-limit.count")) {
                hostingPlayer.sendMessage(chatMessageConfig.getColored("dropevent.error.simultaneous-limit"));
                return false;
        }

        if(config.getBoolean("dropevents.hopper-prevention")) {

            int range = dropevent.getDropRange();
            Location centerLoc = centerLocation.clone();

            for(int x = centerLoc.getBlockX() - range; x <= centerLoc.getBlockX() + range; x++) {
                for(int z = centerLoc.getBlockZ() - range; z <= centerLoc.getBlockZ() + range; z++) {
                    int highestY = centerLoc.getWorld().getHighestBlockYAt(x, z);

                    Location selectedLoc = new  Location(centerLoc.getWorld(), x, highestY, z);

                    Block topBlock = selectedLoc.getBlock();
                    Block lowerBlock = selectedLoc.add(0, -1, 0).getBlock();

                    if(topBlock.getType().equals(Material.HOPPER)
                    || lowerBlock.getType().equals(Material.HOPPER)) {
                        hostingPlayer.sendMessage(chatMessageConfig.getColored("dropevent.error.hopper"));
                        return false;
                    }


                }
            }

            Collection<Entity> nearbyEntities = centerLoc.getWorld().getNearbyEntities(centerLoc, range, 324, range);

            for(Entity entity : nearbyEntities) {
                if(entity instanceof HopperMinecart) {
                    hostingPlayer.sendMessage(chatMessageConfig.getColored("dropevent.error.hopper"));
                    return false;
                }
            }


        }

        if(dropevent.getMinPlayers() > Bukkit.getOnlinePlayers().size()) {

            hostingPlayer.sendMessage(chatMessageConfig
                    .getColored("dropevent.error.player-amount")
                    .replaceText(builder -> { builder
                            .matchLiteral("%p")
                            .replacement(dropevent.getMinPlayers() + "");
            }));

            return false;
        }

        String worldName = centerLocation.getWorld().getKey().asString();
        List<String> forbiddenWorlds = config.getStringList("dropevents.forbidden-worlds");

        if(forbiddenWorlds.contains(worldName)) {
            hostingPlayer.sendMessage(chatMessageConfig.getColored("dropevent.error.forbidden-world"));
            return false;
        }

        return true;
    }

    /**
     *
     * Generates times, when drops occur in course of event relative to starting time.
     *
     * @return List of times
     */
    private List<Double> generateDropTimes() {

        List<Double> times = new ArrayList<>();

        double jitterRange = 0.4;

        int eventTime = dropevent.getEventTimeSec();
        int totalItems = dropevent.getDropCount();

        double interval = (double) eventTime / totalItems;

        for (int i = 0; i < totalItems; i++) {

            double baseTime = i*interval;
            double jitter = (random.nextDouble() - 0.5) * interval * jitterRange;

            times.add(baseTime + jitter);

        }

        Collections.sort(times);

        return times;

    }

    /**
     *
     * Chooses a random position in event perimeter and drops iterm there.
     * Creates firework explostion.
     *
     */
    private void executeDrop() {
        //Generate random position in perimiter
        int range = dropevent.getDropRange();
        double offsetX = (random.nextDouble() * 2 - 1) * range;
        double offsetZ = (random.nextDouble() * 2 - 1) * range;

        Location dropLoc = centerLocation.clone().add(offsetX, 0, offsetZ);
        World world = dropLoc.getWorld();
        if (world == null) {
            return;
        }

        //Determines block, where to spawn the drop (23y over highest)
        int highestY = world.getHighestBlockYAt(dropLoc);
        dropLoc.setY(highestY + 23);

        //Item drop
        ItemStack dropItem = selectRandomItem(dropevent.getDrops());
        world.dropItemNaturally(dropLoc, dropItem);

        //Firework explosion
        Firework fw = createRandomFirework(dropLoc, 0);
        fw.detonate();

    }

    /**
     *
     * Select item from drop pool with given weight.
     *
     * @param drops Drops and there chance in promille.
     * @return
     */
    private ItemStack selectRandomItem(Map<ItemStack, Integer> drops) {
        if (drops == null || drops.isEmpty()) {
            return new ItemStack(Material.AIR);
        }

        int totalChance = 0;
        for (int chance : drops.values()) {
            totalChance += chance;
        }

        int random = (int) (Math.random() * 1000);

        int cumulative = 0;
        for (Map.Entry<ItemStack, Integer> entry : drops.entrySet()) {
            cumulative += entry.getValue();
            if (random < cumulative) {
                return entry.getKey().clone();
            }
        }

        return new ItemStack(Material.AIR);


    }


    private Component basicTranslate(String raw) {
        if (raw == null) return Component.empty();

        String s = raw
                .replace("%c", centerLocation.getBlockX()
                        + ", " + centerLocation.getBlockY()
                        + ", " + centerLocation.getBlockZ())
                .replace("%t", String.valueOf(dropevent.getCountdownSec()))
                .replace("%w", centerLocation.getWorld().getName())
                .replace("%n", dropevent.getName())
                .replace("%l", locationName.isEmpty() ? "" : locationName)
                .replace("%h", hostingPlayer.getName());


        return MiniMessage.miniMessage().deserialize(s);
    }

    /**
     *
     * Translates coded information into the string. Used for broadcasting
     *
     * @param raw String with coded tags
     * @return
     */
    private Component translateBroadDataCodes(String raw) {

        Component base = basicTranslate(raw);

        Component buttonText = basicTranslate(
                chatMessageConfig.get("dropevent.broadcast.tp-prompt.chat")
        );
        Component hoverText = basicTranslate(
                chatMessageConfig.get("dropevent.broadcast.tp-prompt.hover")
        );

        String cmd = "/dropevent tp " + eventId;

        Component tpButton = buttonText
                .hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(ClickEvent.runCommand(cmd));


        return base.replaceText(builder -> builder
                .matchLiteral("%p")
                .replacement(dropevent.isTeleportable() ? tpButton : Component.empty())
        );
    }

    /**
     *
     * Spawns a dying dragon. What did you expect?
     *
     */
    private void spawnDyingDragon() {
        EnderDragon dragon = centerLocation.getWorld().spawn(centerLocation, EnderDragon.class);

        dragon.setMetadata("NO_XP", new FixedMetadataValue(plugin, true));
        dragon.setCollidable(false);
        dragon.setAI(false);
        dragon.setInvisible(true);
        dragon.setHealth(0.0);
    }

    /**
     *
     * Creates a firework with random color and some effect stuff. Won't destroy stuff.
     * This firework automatically takes of with this method call, but the returned object can be modified.
     *
     * @param loc Takeoff postion
     * @param power Flight duration
     * @return Firework entity
     */
    private Firework createRandomFirework(Location loc, int power) {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        Color randomColor = Color.fromRGB(r, g, b);

        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL)
                .withColor(randomColor)
                .withFade(Color.WHITE)
                .flicker(true)
                .trail(true)
                .build();
        meta.addEffect(effect);

        meta.setPower(power);
        firework.setFireworkMeta(meta);

        return firework;
    }




}
