package me.iron.mGine.mod.clientside;

import api.listener.fastevents.GameMapDrawListener;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.world.VoidSystem;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 21.09.2021
 * TIME: 13:55
 * clientside fastutil listener that draws to the map
 */
public class MissionMapDrawer implements GameMapDrawListener {
    private final float sectorScale = 100f/ VoidSystem.SYSTEM_SIZE;
    private final Vector3f halfSectorOffset = new Vector3f(sectorScale/2f,sectorScale/2f,sectorScale/2f);
    MissionClient client;
    public MissionMapDrawer(MissionClient client) {
        super();
        this.client = client;
    }

    @Override
    public void system_PreDraw(GameMapDrawer gameMapDrawer, Vector3i vector3i, boolean b) {

    }

    @Override
    public void system_PostDraw(GameMapDrawer gameMapDrawer, Vector3i vector3i, boolean b) {

    }

    @Override
    public void galaxy_PreDraw(GameMapDrawer gameMapDrawer) {

    }

    @Override
    public void galaxy_PostDraw(GameMapDrawer gameMapDrawer) {

    }

    @Override
    public void galaxy_DrawLines(GameMapDrawer gameMapDrawer) {
        Mission selected = client.selectedMission;
        //will draw colored lines between the tasks (that have a position)
        if (client.selectedMission != null) {
            //draw a line from each checkpoint to its preconditions
            Vector4f color;
            for (MissionTask task: client.selectedMission.getMissionTasks()) {
                if (task.getTaskSector() == null)
                    continue;

                //draw line to each pre condition
                for (MissionTask parent: task.getPreconditions()) {
                    if (parent.getTaskSector() == null)
                        continue;
                    color = getColor(parent.getCurrentState());
                    drawLinesSector(parent.getTaskSector(),task.getTaskSector(),color,grey);
                }
            }
        }

        //draw bright yellow line to selected task
        Vector3i playerSector = GameClientState.instance.getPlayer().getCurrentSector();
        if (client.selectedTask != null && client.selectedTask.getTaskSector() != null && !playerSector.equals(client.selectedTask.getTaskSector())) {
            drawLinesSector(playerSector,client.selectedTask.getTaskSector(),brightYellow,grey);
        }
    }

    @Override
    public void galaxy_DrawSprites(GameMapDrawer gameMapDrawer) {

    }

    @Override
    public void galaxy_DrawQuads(GameMapDrawer gameMapDrawer) {

    }

    private void drawLinesSector(Vector3i from, Vector3i to, Vector4f startColor, Vector4f endColor) {
        Vector3f start = from.toVector3f(); start.scale(sectorScale); start.add(halfSectorOffset);
        Vector3f end = to.toVector3f(); end.scale(sectorScale); end.add(halfSectorOffset);
        DrawUtils.drawFTLLine(start,end,startColor,endColor);
    }

    private static Vector4f darkYellow = new Vector4f(0.5f,0.5f,0,1);
    private static Vector4f brightYellow = new Vector4f(0.97f,1.0f,0,1);
    private static Vector4f darkRed = new Vector4f(0.5f,0f,0,1);
    private static Vector4f brightRed = new Vector4f(1f,0f,0,1);
    private static Vector4f brightGreen = new Vector4f(0,1,0,1);
    private static Vector4f darkGreen = new Vector4f(0,0.5f,0,1);
    private static Vector4f grey = new Vector4f(0.5f,0.5f,0.5f,1);

    /**
     * will select a color based on the missionstate. Open/progress:yellow, failed/aborted: red, success: green
     * @param state
     * @return
     */
    private static Vector4f getColor(MissionState state) {
        switch (state) {
            case OPEN:
                return darkYellow;//dark yellow
            case IN_PROGRESS: //yellow
                return darkYellow;
            case FAILED: //red
                return brightRed;
            case ABORTED: //dark red
                return darkRed;
            case SUCCESS: //dark green
                return darkGreen;
            default:
                return grey;
        }
    }
}