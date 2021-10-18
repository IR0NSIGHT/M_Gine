package me.iron.mGine.mod.missions.tasks;

import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 18.10.2021
 * TIME: 16:03
 */
public class MissionTaskClearSector extends MissionTask {
    private HashSet<SimpleTransformableSendableObject.EntityType> typesToClear = new HashSet<>(); //TODO make serializable
    public MissionTaskClearSector(Mission mission, String name, String info, Vector3i sector, SimpleTransformableSendableObject.EntityType type, boolean optional) {
        super(mission,name,info,optional);
        setTaskSector(sector);
        typesToClear.add(type);
        setIcon(MapIcon.WP_ATTACK);
    }

    @Override
    protected boolean successCondition() {
        return currentState.equals(MissionState.SUCCESS)||isSectorClear();
    }

    private boolean isSectorClear() {
        try {
            if (GameServerState.instance.getUniverse().isSectorLoaded(getTaskSector()))
                return false;
            //test if any unwanted objects exist in sector
            for (SimpleTransformableSendableObject obj: GameServerState.instance.getUniverse().getSector(getTaskSector()).getEntities()) {
                if (typesToClear.contains(obj.getType()))
                    return false;
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void addType(SimpleTransformableSendableObject.EntityType type) {
        typesToClear.add(type);
    }
}
