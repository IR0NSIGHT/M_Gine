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
            MissionTaskScanObject scanSector = new MissionTaskScanObject(this,"scan sector","scan sector"+sector.toString()+" at highest strength",false) {
                private boolean scanned;

                @Override
                public void onScan(EntityScanEvent event) {
                    if (scanned)
                        return;
                    if (!event.getEntity().getSector(new Vector3i()).equals(getTaskSector()))
                        return;
                    if (!scannerRunByParty(event))
                        return;
                    if (!event.isSuccess())
                        return;
                    //success if one of the party scanned this sector.
                     scanned = true;
                    new StarRunnable(){
                        @Override
                        public void run() {
                            unregisterListener();
                        }
                    }.runLater(ModMain.instance,10);
                }

                @Override
                protected boolean successCondition() {
                    return scanned;
                }
            };
            scanSector.setTaskSector(new Vector3i(sector));
            tasks[i] = scanSector;
        }

        this.setMissionTasks(tasks);
        float difficulty = rand.nextFloat();
        float rewardMulti = rand.nextFloat();
        this.description = "Scout " + amountSectors +" sectors by scanning them.";
        this.duration = (int) (60 + 60*(1-difficulty))* amountSectors; //~2 mins per sector
        this.rewardCredits = (int)(300 + 300*difficulty+300*rewardMulti)*1000; //300k base, 300k for difficulty, 300k for random reward
    }

    @Override
    protected void onSuccess() {
        super.onSuccess();
        for (PlayerState p: getActiveParty()) {
            MissionUtil.giveMoney( rewardCredits/getActiveParty().size(),p);
            StringBuilder out = new StringBuilder();
            out.append("Thank you for scouting out these ").append(amountSectors).append(" sectors.\n Take these ");
            out.append(MissionUtil.formatMoney(rewardCredits/getActiveParty().size())).append(" as compensation for your efforts.\nGodspeed pilot!");
            p.sendServerMessage(Lng.astr(out.toString()), ServerMessage.MESSAGE_TYPE_DIALOG);
        }
    }
}
