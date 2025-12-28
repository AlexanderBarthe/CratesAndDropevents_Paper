package dev.upscairs.cratesAndDropevents.dropevents.management;

import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DropEventManager {
    private static final Map<UUID, ActiveDropEvent> active = new ConcurrentHashMap<>();

    public static UUID register(Plugin plugin, Dropevent event, Location center) {
        ActiveDropEvent a = new ActiveDropEvent(event, center);
        active.put(a.getId(), a);

        //Remove after end of event
        long delayTicks = ((event.getCountdownSec() + event.getEventTimeSec()) * 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> active.remove(a.getId()), delayTicks);
        return a.getId();
    }

    public static ActiveDropEvent get(UUID id) {
        return active.get(id);
    }

    public static int getActiveCount() {
        return active.size();
    }

    public static void stop(UUID id) {
        ActiveDropEvent ade = active.get(id);
        if (ade != null) {
            ade.stop();
            active.remove(id);
        }
    }

    public static void stopAll() {
        for (ActiveDropEvent ade : active.values()) {
            if(ade.getBossbarCountdown()!= null) ade.getBossbarCountdown().stop();
            ade.stop();
        }
        active.clear();
    }

    public static Map<UUID, ActiveDropEvent> getActive() {
        return active;
    }
}