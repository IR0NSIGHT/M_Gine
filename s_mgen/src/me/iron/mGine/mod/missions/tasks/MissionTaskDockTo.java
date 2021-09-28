package me.iron.mGine.mod.missions.tasks;

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
    private SegmentController target;
    private boolean wasDocked;
    public MissionTaskDockTo(Mission m, String name, String info, boolean optional, String targetUID) {
        super(m,name,info,optional);
        this.targetUID = targetUID;
    }

    @Override
    protected boolean successCondition() {
        if (wasDocked)
            return true;

        if (target == null) {
            target = getTargetSC();
        }
        
        if (target == null)
            return false;

        //test for all active quest members if they are in a ship, docked to the target
        for (PlayerState p: mission.getActiveParty()) {
            if (p.getFirstControlledTransformableWOExc() instanceof Ship) {
                Ship playerShip = ((Ship)p.getFirstControlledTransformableWOExc());
                if (!playerShip.isDocked())
                    continue;
                if (playerShip.getDockingController().isDockedTo(target)) {
                    wasDocked = true;
                    return true;
                }

            }
        }
        return false;
    }

    private SegmentController getTargetSC() {
        return GameServerState.instance.getSegmentControllersByName().get(targetUID);
    }
}
