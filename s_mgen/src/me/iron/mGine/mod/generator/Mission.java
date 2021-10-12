package me.iron.mGine.mod.generator;

import api.ModPlayground;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.mGine.mod.missions.MissionUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
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
    //active "quest party members"
    private String captain = ""; //captain of mission who controls party.
    private HashSet<String> party = new HashSet<>();

    //generation parameters
    protected MissionType type;
    protected int duration; //in seconds
    protected int rewardCredits;
    protected long seed;
    protected String name = this.getClass().getSimpleName();
    protected String briefing = "briefing goes here."; //short text paragraph with lore and info about mission.
    protected final UUID uuid;
    protected Vector3i sector; //used to determin if a player can see the "OPEN" quest or is to far away. can be null

    //runtime values
    private boolean delayed;
    protected long startTime;
    protected long publishTime; //when was mission made availalbe (for unclaimed garbage collection)

    private transient HashSet<PlayerState> activeParty = new HashSet<>();
    private transient boolean synchFlag; //synch on next update?

    protected MissionState state = MissionState.OPEN;
    protected int remainingTime;

    //checkpoints
    protected transient MissionTask[] missionTasks = new MissionTask[0];

    public Mission(Random rand, long seed) {
        this.uuid = UUID.randomUUID();
        this.publishTime = System.currentTimeMillis();
        this.type = MissionType.getByClass(this.getClass());
        this.seed = seed;
        this.duration = 120+Math.abs(rand.nextInt())%500;
        this.remainingTime = duration;
        this.rewardCredits = Math.abs(rand.nextInt())%1000;
    }

    public Mission(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * sets state to success, runs extra code for special stuff: reward payments etc
     */
    protected void onSuccess() {
        MissionUtil.notifyParty(getActiveParty(),"Mission " + name + " success.",ServerMessage.MESSAGE_TYPE_INFO);
        state = MissionState.SUCCESS;
        flagForSynch();
    }

    /**
     * sets state to failed, runs extra code for special stuff: negative relations with questgiver etc
     */
    protected void onFailure() {
        MissionUtil.notifyParty(getActiveParty(),"Mission " + name + " failed.",ServerMessage.MESSAGE_TYPE_ERROR);
        state = MissionState.FAILED;
        flagForSynch();
    }

    /**
     * mission gets abandoned <=> no more party members left. runs onFailure.
     */
    public void onAbandon() {
        onFailure(); //TODO do something else here
        flagForSynch();
    }

    /**
     * interaction called by party captain: asks for more time in mission. gives 20% extra time for 30% less pay (default)
     */
    public void requestDelay() {
        if (!state.equals(MissionState.IN_PROGRESS))
            return;

        if (delayed) {
            MissionUtil.notifyParty(this.getActiveParty(),"already delayed.",ServerMessage.MESSAGE_TYPE_ERROR);
            return;
        }
        MissionUtil.notifyParty(this.getActiveParty(),"mission was delayed.",ServerMessage.MESSAGE_TYPE_INFO);
        delayed = true;
        duration *= 1.5f;
        rewardCredits *= 0.5f;
        flagForSynch();
    }

    /**
     * runs as long as mission is in progress. tests success and failure conditions
     * @param time
     */
    public void update(long time) {
        if (synchFlag) {
            synchFlag = false;
            for (MissionTask t: missionTasks) {
                ModPlayground.broadcastMessage("task " + t.name + ": " +t.currentState.getName());
            }
            M_GineCore.instance.onMissionUpdate(this);
            ModPlayground.broadcastMessage("synching " + this.getUuid() +"\n"+ this.getName());
        }

        if (state != MissionState.IN_PROGRESS)
            return;

        updateActiveParty();

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

    /**
     * will start the countdown and set the state to in_progress
     * @param time starttime (system time)
     */
    public void start(long time) {
        if (party.size() == 0) //cant start a mission without a party.
            return;
        startTime = time;
        state = MissionState.IN_PROGRESS;
        flagForSynch();
    }

    public String getName() {
        return name;
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
        MissionUtil.notifyParty(getActiveParty(),"Task complete: " + checkpoint.getName(), ServerMessage.MESSAGE_TYPE_INFO);
        System.out.println("Task '"+checkpoint.name+"' " + oldState.getName() +">>" + newState.getName());
        flagForSynch();
    }

    /**
     * add player to this missions party. first member becomes captain. player needs to be online.
     * @param playerName
     */
    public void addPartyMember(String playerName) {
        if (party.contains(playerName))
            return;

        PlayerState p = GameServerState.instance.getPlayerStatesByName().get(playerName);
        if (!canClaim(p)) {
            p.sendServerMessage(Lng.astr("can not claim mission."),ServerMessage.MESSAGE_TYPE_ERROR);
            return;
        }

        if (party.size()==0) {
            setCaptain(playerName);
        }
        party.add(playerName);
        updateActiveParty();
        flagForSynch();
    }

    public void removePartyMember(String playerName) {
        if (!party.contains(playerName))
            return;
        party.remove(playerName);
        updateActiveParty();
        flagForSynch();
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
        flagForSynch();
    }

    public int getDuration() {
        return duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public UUID getUuid() {
        return uuid;
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
        if (this.state.equals(state))
            return;

        this.state = state;
        flagForSynch();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBriefing() {
        return briefing;
    }

    public void setBriefing(String briefing) {
        this.briefing = briefing;
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

    /**
     * flag this mission to be synched when it next updates (before all tests)
     */
    public void flagForSynch() {

        synchFlag = true;
    }

    /**
     * is this mission shown to the player in his GUI?
     * @param p playerstate of player
     * @return true=show mission, false=dont show mission.
     */
    public boolean isVisibleFor(PlayerState p) {
        boolean canSeeOpen = state.equals(MissionState.OPEN) && (getSector() == null || (getSector()!=null && MissionUtil.getDistance(p.getCurrentSector(),getSector())<16));
        return (canSeeOpen || party.contains(p.getName()));
    }

    /**
     * can this player accept/join the mission. default test if in mission sector (if sector !=null).
     * needs to run on client, used by GUI.
     * @param p player
     * @return true or false, you know, a boolean.
     */
    public boolean canClaim(PlayerState p) {
        if (getSector() != null && !p.getCurrentSector().equals(getSector())) {
            return false;
        }
        return true;
    }

    public Vector3i getSector() {
        return sector;
    }

    public void setSector(Vector3i sector) {
        this.sector = sector;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public int getRewardCredits() {
        return rewardCredits;
    }

    public void setRewardCredits(int rewardCredits) {
        this.rewardCredits = rewardCredits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Mission)) return false;
        Mission mission = (Mission) o;
        return uuid.equals(mission.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

