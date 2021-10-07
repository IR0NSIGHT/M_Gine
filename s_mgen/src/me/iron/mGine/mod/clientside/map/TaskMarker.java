package me.iron.mGine.mod.clientside.map;

import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;

import javax.vecmath.Vector4f;
import java.util.HashSet;
import java.util.Objects;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.09.2021
 * TIME: 19:21
 */
public class TaskMarker extends MapMarker {
    private MissionTask task;
    private HashSet<Vector3i> lineTargets = new HashSet<>();

    public TaskMarker(MissionTask task) {
        super(task.getTaskSector(),task.getTaskSummary(),task.getIcon(),MissionMapDrawer.colorByMissionState(task.getCurrentState()));
        this.task = task;
    }

    public void updateFromTask() {
        setSector(task.getTaskSector());
        setColor(MissionMapDrawer.colorByMissionState(task.getCurrentState()));
        name = task.getTaskSummary();
        icon = task.getIcon();
        lineTargets.clear();
        for (int idx: this.task.getPreconditions()) {
            MissionTask task = this.task.mission.getMissionTasks()[idx];
            if (task.getTaskSector() != null) {
                lineTargets.add(task.getTaskSector());
            }
        }
    }

    public void drawLines(MissionMapDrawer drawer) {
        for (Vector3i to: lineTargets) {
            drawer.drawLinesSector(sector,to,new Vector4f(color),new Vector4f(color));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TaskMarker that = (TaskMarker) o;
        return Objects.equals(task, that.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), task);
    }
}
