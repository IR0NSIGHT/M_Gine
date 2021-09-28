package me.iron.mGine.mod.generator; /**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.09.2021
 * TIME: 16:54
 */

import me.iron.mGine.mod.clientside.MapIcon;
import org.schema.common.util.linAlg.Vector3i;

import javax.annotation.Nullable;

/**
 * has to be completed in order to fullfill mission/reach other checkpoints.
 */
public class MissionTask {
    protected MissionState currentState = MissionState.OPEN;
    public Mission mission;
    private Vector3i taskSector;
    protected String name;
    protected String info;
    protected boolean optional; //failing will not result in mission fail
    protected int id; //mission dependent ID (index in array)
    private MapIcon icon; //subsprite that gets displayed on map

    //checkpoints that have to be passed in order to unlock this checkpoint
    private MissionTask[] preconditions = new MissionTask[0];



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
        onInit();
    }

    protected void onInit() {};

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
            int length = preconditions.length;
            for (int i = 0; i<preconditions.length;i++) {
                if (i!=0)
                    out.append(",");
                if (preconditions[i]==null){
                    System.out.println("error");
                }
               out.append(preconditions[i].id);
            }
            out.append("]");
        }
        return out.toString();
    }



    //getters and setters #########################################

    /**
     * gets the tasks sector, may be null!
     * not all tasks have a sector
     * @return
     */
    public Vector3i getTaskSector() {
        return taskSector;
    }

    public void setTaskSector(Vector3i taskSector) {
        this.taskSector = taskSector;
    }

    public MissionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(MissionState currentState) {
        this.currentState = currentState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MissionTask[] getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(MissionTask[] preconditions) {
        this.preconditions = preconditions;
    }

    public MapIcon getIcon() {
        return icon;
    }

    public void setIcon(MapIcon icon) {
        this.icon = icon;
    }
}
