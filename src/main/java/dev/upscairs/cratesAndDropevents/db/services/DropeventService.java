package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.daos.DropeventDao;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.helper.FolderizableElement;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DropeventService {

    private final DropeventDao dao;
    private final CratesAndDropevents plugin;
    private final DropService dropService;


    private final Map<Integer, Dropevent> cache = new ConcurrentHashMap<>();

    private static final long CACHE_REFRESH_DELAY = 20L * 60 * 5;

    public DropeventService(DropService dropService, DropeventDao dao, CratesAndDropevents plugin) {
        this.dao = dao;
        this.plugin = plugin;
        this.dropService = dropService;
        startAutoRefresh(CACHE_REFRESH_DELAY);
    }

    public boolean existsById(int id) {
        return cache.containsKey(id);
    }

    public Dropevent getById(int id) {
        return cache.get(id);
    }

    public Dropevent getById(String idString) {
        try {
            return getById(Integer.parseInt(idString));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<Dropevent> getAll() {
        return cache.values().stream().toList();
    }

    public List<Dropevent> getInFolder(String folderPath) {
        return getAll().stream()
                .filter(e -> Objects.equals(folderPath, e.getFolder()))
                .toList();
    }

    public Set<String> getAllFolderPaths() {
        return getAll().stream()
                .map(FolderizableElement::getFolder)
                .collect(Collectors.toSet());
    }

    public Set<String> getSubfolders(String folderPath) {

        Set<String> subfolders = new HashSet<>();

        int subfolderDepth = folderPath.split("/").length;

        for(String currentFolderPath : getAllFolderPaths()) {
            if(currentFolderPath == null) currentFolderPath = "";
            if(!currentFolderPath.startsWith(folderPath)) continue;

            String[] path =  currentFolderPath.split("/");

            if(path.length > subfolderDepth) {
                String subfolderName = path[subfolderDepth];
                subfolders.add(folderPath + "/" + subfolderName);
            }
        }

        return subfolders;
    }

    public void create(Dropevent event) {
        create(event, null);
    }

    public void create(Dropevent event, Consumer<Dropevent> onCreated) {
        event.setId(0);

        Consumer<Integer> callback = id -> {
            event.setId(id);
            if (event.getFolder() == null) event.setFolder("");
            cache.put(id, event);

            if (onCreated != null) onCreated.accept(event);
        };

        dao.saveDropeventAsync(event, callback);
    }

    public void update(Dropevent event) {
        update(event, null);
    }

    public void update(Dropevent event, Consumer<Integer> onCreated) {

        cache.put(event.getId(), event);

        Consumer<Integer> callback = e -> {
            if(onCreated != null) onCreated.accept(e);
        };

        dao.saveDropeventAsync(event, callback);


    }


    public void delete(int id) {
        dao.deleteDropeventByIdAsync(id);
        cache.remove(id);
        dropService.deleteDropsOfDropevent(id);
    }

    public boolean deleteById(String idString) {
        try {
            boolean existing = existsById(Integer.parseInt(idString));
            if(!existing) return false;

            delete(Integer.parseInt(idString));
            return true;
        } catch (NumberFormatException e) {
            return false;
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
        dao.getAllAsync(events -> {
            cache.clear();
            events.forEach(e -> {
                if (e.getFolder() == null) e.setFolder("");
                cache.put(e.getId(), e);
            });
        });
    }
}
