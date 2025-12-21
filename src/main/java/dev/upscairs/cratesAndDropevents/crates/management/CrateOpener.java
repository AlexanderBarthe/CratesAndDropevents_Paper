package dev.upscairs.cratesAndDropevents.crates.management;

import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class CrateOpener {

    public static void openCrate(Crate crate, Player player, Location location) {

        List<Map.Entry<CrateReward, Integer>> rewards = new ArrayList<>(crate.getRewards().entrySet());

        int pickedNumber = new Random().nextInt(1000);

        for(Map.Entry<CrateReward, Integer> reward : rewards) {

            CrateReward selectedReward = reward.getKey();

            int weight = reward.getValue();

            if (pickedNumber >= weight) {
                pickedNumber -= weight;
                continue;
            }

            if(crate.pittySystemActive() && selectedReward.containsPittiedPlayer(player)) {
                //Pitty system seeks alternative
                CrateReward altReward = findAlternativeReward(crate, reward.getKey());
                if(altReward != null) selectedReward = altReward;
            }

            updatePittyEntries(crate, selectedReward, player);
            CrateStorage.saveCrate(crate);

            selectedReward.execute(player, location);
            return;

        }
    }

    public static CrateReward findAlternativeReward(Crate crate, CrateReward reward) {

        Map<CrateReward, Integer> rewards = crate.getRewards();

        int chance = rewards.get(reward);

        List<CrateReward> alternatives = new ArrayList<>();
        for(Map.Entry<CrateReward, Integer> entry : rewards.entrySet()) {
            if(entry.getValue() == chance && entry.getKey() != reward) {
                alternatives.add(entry.getKey());
            }
        }

        if(alternatives.isEmpty()) return null;

        return alternatives.get(new Random().nextInt(alternatives.size()));

    }

    public static void updatePittyEntries(Crate crate, CrateReward reward, OfflinePlayer player) {

        Map<CrateReward, Integer> rewards = crate.getRewards();

        int chance = rewards.get(reward);

        for(Map.Entry<CrateReward, Integer> entry : rewards.entrySet()) {
            if(entry.getValue() == chance && entry.getKey() != reward) {
                entry.getKey().removePittiedPlayer(player);
            }
        }

        reward.addPittiedPlayer(player);

    }


}
