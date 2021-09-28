package me.iron.mGine.mod.missions;

import api.listener.events.entity.EntityScanEvent;
import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskScanObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.util.Random;
import java.util.Vector;

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
        int amount = 2 + rand.nextInt(5);
        MissionTask[] tasks = new MissionTask[5];
        for (int i = 0; i < amount; i++) {
            sector.set(rand.nextInt()%10,rand.nextInt()%10,rand.nextInt()%10);
            MissionTaskScanObject scanSector = new MissionTaskScanObject(this,"scan sector","scan sector"+sector.toString()+" at highest strength",false) {
                private Vector3i targetSector = new Vector3i(sector);
                private boolean scanned;

                @Override
                public void onScan(EntityScanEvent event) {
                    if (scanned)
                        return;
                    //success if one of the party scanned this sector.
                    if (event.isSuccess() && scannerRunByParty(event) && event.getEntity().getSector(new Vector3i()).equals(sector)) {
                        scanned = true;
                        new StarRunnable(){
                            @Override
                            public void run() {
                                unregisterListener();
                            }
                        }.runLater(ModMain.instance,10);
                    }
                }

                @Override
                protected boolean successCondition() {
                    return scanned;
                }
            };
            tasks[i] = scanSector;
        }

        this.setMissionTasks(tasks);
        float difficulty = rand.nextFloat();
        float rewardMulti = rand.nextFloat();
        this.duration = (int) (60 + 60*difficulty)*amount; //~2 mins per sector
        this.rewardCredits = (int)(300 + 300*difficulty+300*rewardMulti)*1000; //300k base, 300k for difficulty, 300k for random reward
    }

    @Override
    protected void onSuccess() {
        super.onSuccess();
        for (PlayerState p: getActiveParty()) {
            MissionUtil.giveMoney( rewardCredits/getActiveParty().size(),p);
            p.sendServerMessage(Lng.astr("Thank you for scouting out these ",amountSectors," sectors.\n Take these ",MissionUtil.formatMoney(rewardCredits/getActiveParty().size())," as compensation for your efforts.\nGodspeed pilot!"), ServerMessage.MESSAGE_TYPE_DIALOG);
        }
    }
}
