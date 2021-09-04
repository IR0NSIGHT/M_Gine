package me.iron.mGine.mod.generator;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:12
 */
public class Mission {
    //active "quest party members"
    private HashSet<String> party = new HashSet<>();

    public HashSet<String> getParty() {
        return party;
    }

    public void addPartyMember(String playerName) {
        party.add(playerName);
    }

    public void removePartyMember(String playerName) {
        party.remove(playerName);
    }

    public HashSet<PlayerState> getActiveParty() {
        HashSet<PlayerState> active = new HashSet();
        Iterator<String> i = party.iterator();
        PlayerState p = null;
        while (i.hasNext()) {
            p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(i.next());
            if (p!=null)
                active.add(p);
        }
        return active;
    }

    //generation parameters
    protected MissionType type;
    protected int duration;
    protected int rewardCredits;
    protected long seed;
    protected String description = "-";

    //runtime values
    protected long startTime;
    protected MissionState state = MissionState.OPEN;
    protected int remainingTime;

    //checkpoints
    protected MissionTask[] missionTasks = new MissionTask[0];

    public MissionTask[] getMissionTasks() {
        return missionTasks;
    }

    public void setMissionTasks(MissionTask[] missionTasks) {
        this.missionTasks = missionTasks;
        for(int i = 0; i < missionTasks.length; i++) {
            missionTasks[i].id = i;
        }
    }

    public Mission(Random rand, long seed) {
        this.type = MissionType.getByClass(this.getClass());
        this.seed = seed;
        this.duration = 120+Math.abs(rand.nextInt())%500;
        this.remainingTime = duration;
        this.rewardCredits = Math.abs(rand.nextInt())%1000;
    }

    private void onSuccess() {
        System.out.println("MISSION COMPLETE");
        state = MissionState.SUCCESS;
    }

    private void onFailure() {
        System.out.println("MISSION FAILED");
        state = MissionState.FAILED;
    }

    /**
     * runs as long as mission is in progress. tests success and failure conditions
     * @param time
     */
    public void update(long time) {
        if (state != MissionState.IN_PROGRESS)
            return;
        //update countdown
        long runningFor = (time - startTime)/1000;
        remainingTime = (int)(duration-runningFor);

        //update all checkpoints
        for (MissionTask c: missionTasks) {
            c.update();
        }

        if (successCondition())
            onSuccess();

        if (failureCondition())
            onFailure();
    }

    public void start(long time) {
        startTime = time;
        state = MissionState.IN_PROGRESS;
    }

    public String getDescription() {
        StringBuilder out = new StringBuilder();
        out.append(description).append(" reward: ").append(rewardCredits);
        if (state.equals(MissionState.SUCCESS)) {
            out.append(" SUCCESS\n");
        } else if (state.equals(MissionState.FAILED)) {
            out.append(" FAILED\n");
        } else {
            out.append(", remaining time: ").append(String.format("%02d:%02d", (remainingTime % 3600) / 60,remainingTime % 60)).append("\n");
        }
        out.append("tasks:\n");
        for (MissionTask task: missionTasks) {
            out.append(task.getTaskSummary()).append("\n");
        }
        return out.toString();
    }

    boolean successCondition() {
        //success if all non-optional checkpoints are complete.
        for (MissionTask c: missionTasks) {
            if (!c.optional && !c.currentState.equals(MissionState.SUCCESS))
                return false;
        }
        return true;
    };

    /**
     * tests if the mission is being failed
     * @return
     */
    boolean failureCondition() {
        if (remainingTime <= 0)
            return true;
        for (MissionTask c: missionTasks) {
            if (!c.optional && c.currentState.equals(MissionState.FAILED))
                return true;
        }
        return false;
    }

    void onTaskStateChanged(MissionTask checkpoint, MissionState oldState, MissionState newState) {
        System.out.println("Task '"+checkpoint.name+"' " + oldState.getName() +">>" + newState.getName());
    }
}

