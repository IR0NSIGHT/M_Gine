package me.iron.mGine.mod.generator;

import api.DebugFile;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.mGine.mod.missions.MissionUtil;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.server.ServerMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:12
 */
public class Mission implements Serializable {
    private int missionID;

    //active "quest party members"
    private String captain = ""; //captain of mission who controls party.
    private HashSet<String> party = new HashSet<>();

    //generation parameters
    protected MissionType type;
    protected int duration; //in seconds
    protected int rewardCredits;
    protected long seed;
    protected String description;
    protected final UUID uuid;

    //runtime values
    protected long startTime;
    private transient HashSet<PlayerState> activeParty = new HashSet<>();

    protected MissionState state = MissionState.OPEN;
    protected int remainingTime;

    //checkpoints
    protected transient MissionTask[] missionTasks = new MissionTask[0];

    public Mission(Random rand, long seed) {
        this.uuid = UUID.randomUUID();
        this.type = MissionType.getByClass(this.getClass());
        this.seed = seed;
        this.duration = 120+Math.abs(rand.nextInt())%500;
        this.remainingTime = duration;
        this.rewardCredits = Math.abs(rand.nextInt())%1000;
        missionID = M_GineCore.getNextID();
    }

    /**
     * sets state to success, runs extra code for special stuff: reward payments etc
     */
    protected void onSuccess() {
        System.out.println("MISSION COMPLETE");
        state = MissionState.SUCCESS;
    }

    /**
     * sets state to failed, runs extra code for special stuff: negative relations with questgiver etc
     */
    protected void onFailure() {
        System.out.println("MISSION FAILED");
        state = MissionState.FAILED;

    }

    /**
     * mission gets abandoned <=> no more party members left. runs onFailure.
     */
    public void onAbandon() {
        onFailure();
    }

    /**
     * interaction called by party captain: asks for more time in mission. gives 20% extra time for 30% less pay (default)
     */
    public void requestDelay() {
        duration *= 1.02f;
        rewardCredits *= 0.7f;
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

        M_GineCore.instance.onMissionUpdate(this);
    }

    /**
     * will start the countdown and set the state to in_progress
     * @param time starttime (system time)
     */
    public void start(long time) {
        startTime = time;
        state = MissionState.IN_PROGRESS;
    }

    public String getDescription() {
        StringBuilder out = new StringBuilder();
        out.append(description).append("(").append(getIDString()).append(")").append(" reward: ").append(MissionUtil.formatMoney(rewardCredits));
        out.append(" state: ").append(state).append("\n");
        out.append("remaining time: ").append(String.format("%02d:%02d:%02d",(remainingTime % (60*60*60))/(60*60),(remainingTime % 3600) / 60,remainingTime % 60)).append("\n");
        out.append("tasks:\n");
        for (MissionTask task: missionTasks) {
            out.append(task.getTaskSummary()).append("\n");
        }
        return out.toString();
    }

    protected boolean successCondition() {
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
    protected boolean failureCondition() {
        if (remainingTime <= 0)
            return true;
        for (MissionTask c: missionTasks) {
            if (!c.optional && c.currentState.equals(MissionState.FAILED))
                return true;
        }
        return false;
    }

    protected void onTaskStateChanged(MissionTask checkpoint, MissionState oldState, MissionState newState) {
        if (!oldState.equals(MissionState.SUCCESS) && newState.equals(MissionState.SUCCESS)) {
            MissionUtil.notifyParty(getActiveParty(),"Task complete: " + checkpoint.getName(), ServerMessage.MESSAGE_TYPE_INFO);
        }
        System.out.println("Task '"+checkpoint.name+"' " + oldState.getName() +">>" + newState.getName());
    }

    /**
     * add player to this missions party. first member becomes captain.
     * @param playerName
     */
    public void addPartyMember(String playerName) {
        if (party.size()==0) {
            setCaptain(playerName);
        }
        party.add(playerName);
        updateActiveParty();
    }

    public void removePartyMember(String playerName) {
        party.remove(playerName);
        updateActiveParty();
    }

    public void updateActiveParty() {
        activeParty.clear();
        Iterator<String> i = party.iterator();
        PlayerState p;
        while (i.hasNext()) {
            p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(i.next());
            if (p!=null)
                activeParty.add(p);
        }
    }

    /**
     * write this mission object into the buffer for serialization.
     * @param buffer
     */
    public void writeToBuffer(PacketWriteBuffer buffer) throws IOException {
        //write self with primitive values
        buffer.writeString(getClass().getName());
        buffer.writeObject(this);

       //DebugFile.log("buffer wrote mission class" + getClass().getSimpleName());
       //DebugFile.log("buffer wrote mission obj" + uuid);

        //write non primitive objects
        buffer.writeInt(missionTasks.length);
      //  DebugFile.log("buffer wrote task list size" + missionTasks.length);
        for (MissionTask task: missionTasks) {
            task.writeToBuffer(buffer);
        }
    }

    public void readFromBuffer(PacketReadBuffer buffer) throws IOException, ClassNotFoundException {
        //primitive values exist
        int size = buffer.readInt();

        //get mission tasks from buffer
        missionTasks = new MissionTask[size];
        for (int i = 0; i < size; i++) {
            Class clazz = Class.forName(buffer.readString());
            Object taskObj = buffer.readObject(clazz);
            if (!(taskObj instanceof MissionTask))
                continue;
            MissionTask task =(MissionTask) taskObj; //target task broken.
            task.readFromBuffer(buffer);
            task.mission = this;
            missionTasks[i] = task;
        }
    }

    //getters and setters
    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIDString() {
        return Integer.toOctalString(missionID);
    }

    public HashSet<String> getParty() {
        return party;
    }

    public HashSet<PlayerState> getActiveParty() {
        return activeParty;
    }

    public MissionType getType() {
        return type;
    }

    public MissionState getState() {
        return state;
    }

    public void setState(MissionState state) {
        this.state = state;
    }

    public MissionTask[] getMissionTasks() {
        return missionTasks;
    }

    /**
     * set tasks for this mission
     * @param missionTasks array with non-null elements. fill array before using this method!
     */
    public void setMissionTasks(MissionTask[] missionTasks) {
        this.missionTasks = missionTasks;
        for(int i = 0; i < missionTasks.length; i++) {
            missionTasks[i].id = i;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mission mission = (Mission) o;
        return uuid.equals(mission.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

