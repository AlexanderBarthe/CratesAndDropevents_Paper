package dev.upscairs.cratesAndDropevents.crates.management;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.crates.rewards.CrateReward;
import dev.upscairs.cratesAndDropevents.db.services.CrateRewardService;
import dev.upscairs.cratesAndDropevents.db.services.PlayerPityService;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrateOpener {

    private final CrateRewardService rewardService;
    private final PlayerPityService pityService;

    public CrateOpener(CratesAndDropevents plugin) {
        this.rewardService = plugin.getDbServices().getCrateRewardService();
        this.pityService = plugin.getDbServices().getPlayerPityService();
    }


    public void openCrate(Crate crate, Player player, Location location) {

        List<CrateReward> rewards = new ArrayList<>(rewardService.getRewardsForCrate(crate.getId()));

        int pickedNumber = new Random().nextInt(1000);

        for(CrateReward reward : rewards) {

            int weight = reward.getProbability();

            if (pickedNumber >= weight) {
                pickedNumber -= weight;
                continue;
            }

            if(crate.pitySystemActive() && pityService.isPitied(reward.getId(), player)) {
                //Pity system seeks alternative
                CrateReward altReward = findAlternativeReward(crate, reward);
                if(altReward != null) reward = altReward;
            }

            updatePityEntries(crate, reward, player);

            reward.execute(player, location);
            return;

        }
    }

    public CrateReward findAlternativeReward(Crate crate, CrateReward reward) {

        List<CrateReward> rewards = rewardService.getRewardsForCrate(crate.getId());

        int chance = reward.getProbability();

        List<CrateReward> alternatives = new ArrayList<>();
        for(CrateReward altCandidate : rewards) {
            if(altCandidate.getProbability() == chance && altCandidate != reward) {
                alternatives.add(altCandidate);
            }
        }

        if(alternatives.isEmpty()) return null;

        return alternatives.get(new Random().nextInt(alternatives.size()));

    }

    public void updatePityEntries(Crate crate, CrateReward newReward, OfflinePlayer player) {

        List<CrateReward> rewards = rewardService.getRewardsForCrate(crate.getId());

        int chance = newReward.getProbability();

        for(CrateReward oldRewardCandidate : rewards) {
            if(oldRewardCandidate.getProbability() == chance && oldRewardCandidate != newReward) {
                pityService.removePlayerPity(oldRewardCandidate.getId(), player);
            }
        }

        pityService.addPitiedPlayer(newReward.getId(), player);

    }


}
