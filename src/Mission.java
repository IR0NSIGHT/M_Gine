import com.sun.javafx.geom.Vec3f;

import java.util.HashMap;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:12
 */
public class Mission {
    //generation parameters
    MissionType type;
    int duration;
    int rewardCredits;
    long seed;
    String description = "-";

    //runtime values
    long startTime;
    MissionState state = MissionState.OPEN;
    int remainingTime;

    //checkpoints
    MissionTask[] missionTasks = new MissionTask[0];

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
        this.duration = 5+Math.abs(rand.nextInt())%55;
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
        remainingTime = (int)(duration-(time - startTime));

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
            out.append(", remaining time: ").append(remainingTime).append("\n");
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
        if (remainingTime <= 0) return true;
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

/**
 * available missiontypes, lists their classes
 */
enum MissionType {
    TRANSPORT_GOODS(MissionTransportGoods.class,"transport goods");

    /*
    FERRY_PASSENGER(null,"Ferry passengers"),
    DELIVER_GOODS(null,"deliver goods"),
    SCAN_OBJECTS(null,""),
    DESTROY_OBJECTS(null,""),
    PROTECT_OBJECTS(null,"");
    */

    //enum parameters
    Class missionClass;
    String name;

    //constructor
    private MissionType(Class<? extends Mission> missionClass, String name) {
        this.missionClass = missionClass;
        this.name = name;
    }

    //generate a mission from this type
    public Mission generate(Random rand, long seed) {
        switch (this) {
            case TRANSPORT_GOODS: {
                System.out.println("transport stuff");
                return new MissionTransportGoods(rand, seed);
            }
        }
        return null;
    }

    /**
     * get missiontype by its index, is out-of-bounds safe (abs + modulo)
     * @param idx
     * @return type at that index
     */
    static MissionType getByIdx(int idx) {
        idx = Math.abs(idx)%values().length;
        return values()[idx];
    }

    //maps
    static HashMap<Class,MissionType> BY_CLASS = new HashMap<>();
    //not null safe!
    public static MissionType getByClass(Class mClass) {
        return BY_CLASS.get(mClass);
    }

    //fill map
    static {
        for (MissionType t: MissionType.values()) {
            BY_CLASS.put(t.missionClass,t);
        }
    }


}

enum MissionState {
    OPEN("open"),
    IN_PROGRESS("in progress"),
    SUCCESS("success"),
    FAILED("failed"),
    ABORTED("aborted");

    private String name;
    private MissionState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
