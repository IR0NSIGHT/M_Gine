package me.iron.mGine.mod.missions;


import me.iron.mGine.mod.clientside.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.09.2021
 * TIME: 14:10
 */
public class MissionPatrolSectors extends Mission {
    int cargoAmount;
    float completionRadius;
    float distanceTotal;
    Vector3i center;
    public MissionPatrolSectors(final Random rand, long seed, Vector3i center) {
        super(rand,seed);
        this.center = center;
        cargoAmount = 20 + Math.abs(rand.nextInt())%80;
        completionRadius = 0.5f;
        description = "Patrol sectors";
        int waypoints = 4 + Math.abs(rand.nextInt())%6;
        final MissionTask[] tasks = new MissionTask[waypoints];

        for (int i = 0; i < waypoints;i++) {
            Vector3i sectorTemp = new Vector3i(
                    rand.nextInt()%VoidSystem.SYSTEM_SIZE,
                    rand.nextInt()%VoidSystem.SYSTEM_SIZE,
                    rand.nextInt()%VoidSystem.SYSTEM_SIZE
             );
            sectorTemp.add(center);
            MissionTask move = new MissionTask(this,"move","go to sector " + sectorTemp.toString()) {
               boolean visited;

                @Override
               protected boolean successCondition() {
                   if (visited)
                       return true;
                   for (PlayerState p: mission.getActiveParty()) {
                       if (getTaskSector() != null && p.getCurrentSector().equals(getTaskSector())) {
                           visited = true;
                           return true;
                       }
                   }
                   return false;
               }
            };
            move.setIcon(MapIcon.WP_MOVE);
            move.setTaskSector(sectorTemp);
            if (i > 0) {
                MissionTask[] precond = new MissionTask[]{tasks[i-1]};
                move.setPreconditions(precond);
            }
            tasks[i] = move;
            if (i > 0) {
                Vector3f dist = tasks[i].getTaskSector().toVector3f();
                dist.sub(tasks[i-1].getTaskSector().toVector3f());
                distanceTotal += dist.length()* VoidSystem.SYSTEM_SIZE;
            }

        }
        int minutesNeeded = (int) (distanceTotal/36);
        this.rewardCredits = minutesNeeded * (50000 + Math.abs(rand.nextInt())%50000);
        this.duration = (int) (minutesNeeded * 60 * (2+Math.abs(rand.nextFloat())%3));
        this.setMissionTasks(tasks);
    }

    @Override
    public void update(long time) {
        super.update(time);
    }

    @Override
    protected void onTaskStateChanged(MissionTask checkpoint, MissionState oldState, MissionState newState) {
        super.onTaskStateChanged(checkpoint, oldState, newState);
        if (!newState.equals(MissionState.SUCCESS))
            return;

        for (PlayerState p: getActiveParty()) {
            //inform about checkpoint
            for (int i = 0; i < 6; i++) {
                p.sendServerMessage(Lng.astr("Task '"+checkpoint.getName() +"' complete ("+this.getIDString()+")."),2);
            }
        }
        notifyObservers();
    }

    @Override
    protected void onSuccess() {
        StringBuilder b = new StringBuilder();
        b.append("You have completed Patrol ").append(getIDString()).append(".").append(rewardCredits)
        .append(" credits have been added to your account. Thank you for your service");
        String mssg = b.toString();
        for (PlayerState p: getActiveParty()) {
            MissionUtil.giveMoney(rewardCredits,p);
            p.sendServerMessage(Lng.astr(mssg), ServerMessage.MESSAGE_TYPE_DIALOG);
        }
        super.onSuccess();
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\ntotal distance: " + Math.round(distanceTotal) + "km";
    }

    @Override
    public String toString() {
        return "MissionTransportGoods{" +"\n"+
                ", cargoAmount=" + cargoAmount +"\n"+
                ", completionRadius=" + completionRadius +"\n"+
                ", type=" + type +"\n"+
                ", duration=" + duration +"\n"+
                ", rewardCredits=" + rewardCredits +"\n"+
                ", seed=" + seed +"\n"+
                ", startTime=" + startTime +"\n"+
                ", state=" + state +"\n"+
                ", remainingTime=" + remainingTime +"\n"+
                '}';
    }
}
