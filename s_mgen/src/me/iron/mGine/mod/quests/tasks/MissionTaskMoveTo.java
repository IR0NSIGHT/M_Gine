package me.iron.mGine.mod.quests.tasks;

import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 07.10.2021
 * TIME: 22:37
 */
public class MissionTaskMoveTo extends MissionTask {
    boolean visited;

    public MissionTaskMoveTo(Mission mission, String name, String info, Vector3i sector, boolean optional) {
        super(mission, name, info, optional);
        setTaskSector(sector);
        setIcon(MapIcon.WP_MOVE);
    }

    @Override
    protected boolean successCondition() {
        if (visited || currentState.equals(MissionState.SUCCESS))
            return true;
        for (PlayerState p: mission.getActiveParty()) {
            if (getTaskSector() != null && p.getCurrentSector().equals(getTaskSector())) {
                visited = true;
                return true;
            }
        }
        return false;
    }
}
