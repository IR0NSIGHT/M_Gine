package me.iron.mGine.mod.missions;


import me.iron.mGine.mod.generator.LoreGenerator;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskMoveTo;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;

import javax.vecmath.Vector3f;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public MissionPatrolSectors(final Random rand, long seed) {
        super(rand,seed);
        Vector3i center = new Vector3i(0,rand.nextInt(69),69);
        try {
            if (DataBaseManager.instance.getNPCFactions().size() == 0) {
                new NullPointerException("NO NPC FACTIONS DETECTED").printStackTrace();
                return;
            }
            NPCFaction f = DataBaseManager.instance.getNPCFactions().get(rand.nextInt(DataBaseManager.instance.getNPCFactions().size()));
            clientFactionID = f.getIdFaction();
            clientFactionName = f.getName();
            ArrayList<DataBaseSystem> systems = DataBaseManager.instance.getSystems(f.getIdFaction());
            DataBaseStation s = DataBaseManager.instance.getRandomStation(systems,null, SpaceStation.SpaceStationType.FACTION,rand.nextLong());
            if (s != null) {
                center = s.getPosition();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        this.center = center;
        setSector(center);
        cargoAmount = 20 + Math.abs(rand.nextInt())%80;
        completionRadius = 0.5f;
        name = "Patrol sectors for " + clientFactionName;
        this.briefing = LoreGenerator.instance.enemySpottedNearby(clientFactionID,rand.nextLong()) + "\n Patrol these sectors and engage any enemy craft.";

        int waypoints = 4 + rand.nextInt(4);
        final MissionTask[] tasks = new MissionTask[waypoints];

        for (int i = 0; i < waypoints;i++) {
            Vector3i sectorTemp = new Vector3i(
            rand.nextInt()%VoidSystem.SYSTEM_SIZE*0.75f,
            rand.nextInt()%VoidSystem.SYSTEM_SIZE*0.75f,
            rand.nextInt()%VoidSystem.SYSTEM_SIZE*0.75f
             );
            sectorTemp.add(center);
            MissionTask move = new MissionTaskMoveTo(this,"move","go to sector " + sectorTemp.toString(),sectorTemp,false);
            if (i > 0) {
                int[] precond = new int[]{i-1};
                move.setPreconditions(precond);
            } else {
                setSector(sectorTemp); //make this the starting sector == first move WP
            }
            tasks[i] = move;
            if (i > 0) {
                Vector3f dist = tasks[i].getTaskSector().toVector3f();
                dist.sub(tasks[i-1].getTaskSector().toVector3f());
                distanceTotal += dist.length()* GameServerState.instance.getSectorSize();
            }
        }
        float secondsNeeded = MissionUtil.estimateTimeByDistance(distanceTotal,0.75f);
        this.rewardCredits = MissionUtil.calculateReward(secondsNeeded,3,1,rand.nextLong());
        this.duration = (int)(secondsNeeded * 1f+0.3f* rand.nextFloat());

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
                p.sendServerMessage(Lng.astr("Task '"+checkpoint.getName() +"' complete ("+"MISSION NAME GOES HERE"+")."),2);
            }
        }
    }

    @Override
    protected void onSuccess() {
        super.onSuccess();
    }

    @Override
    public String getSuccessText() {
        StringBuilder b = new StringBuilder();
        b.append("You have completed Patrol ").append(getUuid().toString()).append(". \n"); //TODO proper name generation
        b.append("Your reward of ").append(MissionUtil.formatMoney(rewardCredits))
                .append(" has been added to your account. Thank you for your service");
        for (PlayerState p: getActiveParty()) {
            MissionUtil.giveMoney(rewardCredits,p);
        }
        return b.toString();
    }

    @Override
    public String toString() {
        return "MissionPatrolSectors{" +
                "cargoAmount=" + cargoAmount +
                ", completionRadius=" + completionRadius +
                ", distanceTotal=" + distanceTotal +
                ", center=" + center +
                ", type=" + type +
                ", duration=" + duration +
                ", rewardCredits=" + rewardCredits +
                ", seed=" + seed +
                ", description='" + name + '\'' +
                ", uuid=" + uuid +
                ", startTime=" + startTime +
                ", state=" + state +
                ", remainingTime=" + remainingTime +
                ", missionTasks=" + Arrays.toString(missionTasks) +
                '}';
    }
}
