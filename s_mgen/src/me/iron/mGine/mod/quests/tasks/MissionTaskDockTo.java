package me.iron.mGine.mod.quests.tasks;

import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.09.2021
 * TIME: 23:09
 * dock to given target. completed if any of the questparty is docked to target
 */
public class MissionTaskDockTo extends MissionTask {
    private String targetUID; //UID of target to dock to
    private transient SegmentController targetObject; //runtime reference to target ship/station.
    private boolean wasDocked; //task was completed at some point

    /**
     * will create task that completes once on of the missionparty docks to the target. non reversible.
     * @param m mission
     * @param name name of task
     * @param info info of task
     * @param optional is optional
     * @param targetUID UID of target to dock to
     */
    public MissionTaskDockTo(Mission m, String name, String info, boolean optional, String targetUID) {
        super(m,name,info,optional);
        this.targetUID = targetUID;
    }

    @Override
    protected boolean successCondition() {
        if (targetObject == null) {
            targetObject = getTargetSC();
        }

        if (targetObject == null)
            return false;

        //test for all active quest members if they are in a ship, docked to the target
        for (PlayerState p: mission.getActiveParty()) {
            if (p.getFirstControlledTransformableWOExc() instanceof Ship) {
                Ship playerShip = ((Ship)p.getFirstControlledTransformableWOExc());
                if (!playerShip.isDocked())
                    continue;
                if (playerShip.railController.isAnyChildOf(targetObject)) {
                    wasDocked = true;
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    protected boolean failureCondition() {
        //TODO target was destroyed
        return false;
    }

    @Override
    public void update() {
        if (wasDocked) //dont update anymore once task completed (non-reversible)
            return;
        super.update();
    }

    private SegmentController getTargetSC() {
        return GameServerState.instance.getSegmentControllersByName().get(targetUID);
    }
    public void setTargetUID(String UID) {
        targetUID = UID;
    }

    public String getTargetUID() {
        return targetUID;
    }
}
