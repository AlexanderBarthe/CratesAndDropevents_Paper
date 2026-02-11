package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.daos.DropDao;
import dev.upscairs.cratesAndDropevents.dropevents.Drop;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DropService {

    private final DropDao dao;
    private final CratesAndDropevents plugin;

    private final Map<Integer, Drop> cache = new ConcurrentHashMap<>();

    private static final long CACHE_REFRESH_DELAY = 20L * 60 * 5;

    public DropService(DropDao dao, CratesAndDropevents plugin) {
        this.dao = dao;
        this.plugin = plugin;
        startAutoRefresh(CACHE_REFRESH_DELAY);
    }

    public boolean existsById(int id) {
        return cache.containsKey(id);
    }

    public Drop getDropById(int id) {
        return cache.get(id);
    }

    public List<Drop> getAllDrops() {
        return cache.values().stream().toList();
    }

    public List<Drop> getDropsForDropevent(int dropeventId) {
        return cache.values().stream()
                .filter(d -> d.getDropeventId() == dropeventId)
                .toList();
    }

    public int getRemainingChanceForEvent(int dropeventId) {
        return 1000 - getDropsForDropevent(dropeventId).stream().mapToInt(Drop::getProbability).sum();
    }

    public void createDrop(Drop drop, Consumer<Drop> onCreated) {
        drop.setId(0);

        Consumer<Integer> daoCallback = id -> {
            drop.setId(id);
            cache.put(id, drop);

            if (onCreated != null) {
                try {
                    onCreated.accept(drop);
                } catch (Throwable t) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "onCreated callback failed", t);
                }
            }
        };

        dao.saveDropAsync(drop, daoCallback);
    }

    public void createDrop(Drop drop) {
        createDrop(drop, null);
    }

    public void updateDrop(Drop drop) {
        dao.saveDropAsync(drop, null);
        cache.put(drop.getId(), drop);
    }

    public void deleteDropById(int id) {
        dao.deleteDropByIdAsync(id);
        cache.remove(id);
    }


    public void deleteDropsOfDropevent(int dropeventId) {
        List<Drop> drops = getDropsForDropevent(dropeventId);
        for (Drop d : drops) {
            dao.deleteDropByIdAsync(d.getId());
            cache.remove(d.getId());
        }
    }

    private void startAutoRefresh(long intervalTicks) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshCacheAsync,
                40,
                intervalTicks
        );
    }

    public void refreshCacheAsync() {
        dao.getAllAsync(drops -> {
            cache.clear();
            drops.forEach(drop -> cache.put(drop.getId(), drop));
        });
    }
}
