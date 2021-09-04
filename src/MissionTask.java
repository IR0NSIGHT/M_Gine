/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.09.2021
 * TIME: 16:54
 */

/**
 * has to be completed in order to fullfill mission/reach other checkpoints.
 */
public class MissionTask {
    MissionState currentState = MissionState.OPEN;
    Mission mission;
    String name;
    String info;
    boolean optional; //failing will not result in mission fail
    int id; //mission dependent ID (index in array)
    //checkpoints that have to be passed in order to unlock this checkpoint
    private MissionTask[] preconditions = new MissionTask[0];

    public MissionTask[] getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(MissionTask[] preconditions) {
        this.preconditions = preconditions;
    }

    public MissionTask(Mission mission, String name, String info) {
        this.info = info;
        this.mission = mission;
        this.name = name;
    }

    public MissionTask(Mission mission, String name, String info, boolean optional) {
        this.info = info;
        this.mission = mission;
        this.optional = optional;
        this.name = name;
    }

    protected boolean successCondition() {return false;}
    protected boolean failureCondition() {return false;}
    protected boolean preconditionsSatisfied() {
        for (MissionTask x: preconditions) {
            if (x.currentState != MissionState.SUCCESS)
                return false; //can not be done yet bc condition isnt met
        }
        return true;
    }

    protected void onStateChanged(MissionState oldState, MissionState newState) {
        mission.onTaskStateChanged(this,oldState,newState);
    }

    public void update() {
        MissionState previous = currentState;
        if (!preconditionsSatisfied()) {
            currentState = MissionState.OPEN;
            if (previous != currentState)
                this.onStateChanged(previous,currentState);
            return;
        }
        if (failureCondition()) {
            currentState = MissionState.FAILED;
            if (previous != currentState)
                this.onStateChanged(previous,currentState);
            return;
        }
        if (successCondition()) {
            currentState = MissionState.SUCCESS;
            if (previous != currentState)
                this.onStateChanged(previous,currentState);
            return;
        }

        this.currentState = MissionState.IN_PROGRESS;
        if (previous != currentState)
            this.onStateChanged(previous,currentState);
    }

    public String getTaskSummary() {
        StringBuilder out = new StringBuilder();
        out.append(id).append("   ").append(info).append(" [").append(currentState).append("]");
        if (optional)
            out.append(" (optional)");
        if (preconditions.length >0) {
            out.append("[");
            for (int i = 0; i<preconditions.length;i++) {
                if (i!=0)
                    out.append(",");
               out.append(preconditions[i].id);
            }
            out.append("]");
        }
        return out.toString();
    }
}
