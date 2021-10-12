package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskScanSector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.util.Random;

/**
* STARMADE MOD
* CREATOR: Max1M
* DATE: 22.09.2021
* TIME: 00:47
*/public class MissionScout extends Mission {
    private int amountSectors;
    public MissionScout(Random rand, long seed, Vector3i center) {
        super(rand,seed);
        final Vector3i sector = new Vector3i(center);
        amountSectors = 2 + rand.nextInt(5);
        MissionTask[] tasks = new MissionTask[amountSectors];
        for (int i = 0; i < amountSectors; i++) {
            sector.set(rand.nextInt()%10,rand.nextInt()%10,rand.nextInt()%10);
            MissionTaskScanSector scanSector = new MissionTaskScanSector(this,"scan sector","scan sector"+sector.toString()+" at highest strength", new Vector3i(sector),false);
            tasks[i] = scanSector;
        }

        this.setMissionTasks(tasks);
        float difficulty = rand.nextFloat();
        float rewardMulti = rand.nextFloat();
        this.name = "Scout " + amountSectors +" sectors by scanning them.";
        this.duration = (int) (60 + 60*(1-difficulty))* amountSectors; //~2 mins per sector
        this.rewardCredits = (int)(300 + 300*difficulty+300*rewardMulti)*1000; //300k base, 300k for difficulty, 300k for random reward
    }

    @Override
    protected void onSuccess() {
        super.onSuccess();
        for (PlayerState p: getActiveParty()) {
            MissionUtil.giveMoney( rewardCredits/getActiveParty().size(),p);
            String out = "Thank you for scouting out these " + amountSectors + " sectors.\n Take these " +
                    MissionUtil.formatMoney(rewardCredits / getActiveParty().size()) + " as compensation for your efforts.\nGodspeed pilot!";
            p.sendServerMessage(Lng.astr(out), ServerMessage.MESSAGE_TYPE_DIALOG);
        }
    }
}
