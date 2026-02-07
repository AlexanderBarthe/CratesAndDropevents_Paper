package dev.upscairs.cratesAndDropevents.db.services;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.management.Crate;
import dev.upscairs.cratesAndDropevents.db.daos.CrateDao;
import dev.upscairs.cratesAndDropevents.helper.FolderizableElement;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CrateService {

    private final CrateRewardService rewardService;
    private final CrateDao dao;
    private final CratesAndDropevents plugin;

    private final Map<Integer, Crate> crateCache = new ConcurrentHashMap<>();

    private static final long CACHE_REFRESH_DELAY = 20L * 60 * 5;

    public CrateService(CrateRewardService rewardService, CrateDao rewardDao, CratesAndDropevents plugin) {
        this.rewardService = rewardService;
        this.dao = rewardDao;
        this.plugin = plugin;
        startAutoRefresh(CACHE_REFRESH_DELAY);
    }

    public boolean existsById(int id) {
        return crateCache.containsKey(id);
    }

    public Crate getCrateById(int id) {
        return crateCache.get(id);
    }

    public Crate getCrateById(String idString) {
        try {
            return getCrateById(Integer.parseInt(idString));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<Crate> getAllCrates() {
        return crateCache.values().stream().toList();
    }

    public List<Crate> getCratesInFolder(String folderPath) {
        return getAllCrates().stream().filter(crate -> crate.getFolder().equals(folderPath)).toList();
    }

    public Set<String> getAllFolderPaths() {
        return getAllCrates().stream().map(FolderizableElement::getFolder).collect(Collectors.toSet());
    }

    public Set<String> getSubfolders(String folderPath) {

        Set<String> subfolders = new HashSet<>();

        int subfolderDepth = folderPath.split("/").length;

        for(String currentFolderPath : getAllFolderPaths()) {
            if(!currentFolderPath.startsWith(folderPath)) continue;

            String[] path =  currentFolderPath.split("/");

            if(path.length > subfolderDepth) {
                String subfolderName = path[subfolderDepth];
                subfolders.add(folderPath + "/" + subfolderName);
            }
        }

        return subfolders;
    }

    public void createCrate(Crate crate) {
        createCrate(crate, null);
    }

    public void createCrate(Crate crate, Consumer<Crate> onCreated) {
        crate.setId(0);

        Consumer<Integer> callback = id -> {
            crate.setId(id);
            crateCache.put(id, crate);

            if(onCreated != null) onCreated.accept(crate);

        };

        dao.saveCrateAsync(crate, callback);
    }

    public void updateCrate(Crate crate) {
        dao.saveCrateAsync(crate, null);
        crateCache.put(crate.getId(), crate);
    }

    public void deleteCrateById(int id) {
        dao.deleteCrateByIdAsync(id);
        crateCache.remove(id);
        rewardService.deleteRewardsOfCrate(id);
    }

    public boolean deleteById(String idString) {
        try {
            boolean existing = existsById(Integer.parseInt(idString));
            if(!existing) return false;

            deleteCrateById(Integer.parseInt(idString));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void startAutoRefresh(long intervalTicks) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshCacheAsync,
                40, intervalTicks);
    }

    public void refreshCacheAsync() {
        Consumer<List<Crate>> consumer = crates -> {
            crateCache.clear();
            crates.forEach(crate -> crateCache.put(crate.getId(), crate));
        };
        dao.getAllAsync(consumer);
    }

}
