package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskClearSector;
import me.iron.mGine.mod.missions.tasks.MissionTaskMoveTo;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.simulation.npc.NPCFaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 18.10.2021
 * TIME: 14:49
 */
public class MissionDestroyStation extends Mission {
    private DataBaseStation target;
    public MissionDestroyStation(Random rand, long seed) {
        super(rand, seed);
        try {
        //get a startpoint in NPC territory/get client
            NPCFaction client = DataBaseManager.instance.getNPCFactions().get(rand.nextInt(DataBaseManager.instance.getNPCFactions().size()));
            DataBaseStation s = DataBaseManager.instance.getRandomStation(
                    DataBaseManager.instance.getSystems(client.getIdFaction()),
                    null,
                    SpaceStation.SpaceStationType.FACTION,
                    rand.nextLong()
                    );
            if (s == null || s.getPosition() == null) {
                new NullPointerException().printStackTrace();
                return;
            }
            clientFactionID = client.getIdFaction();
            clientFactionName = client.getName();
            setSector(s.getPosition());
            MissionTask[] tasks = new MissionTask[2];
            MissionTask moveToStart = new MissionTaskMoveTo(this,"speak with the representative of " + clientFactionName,"speak with the representative of " + clientFactionName,s.getPosition(),false);
            tasks[0] = moveToStart;

        //select a pirate station randomly across galaxy
            ArrayList<DataBaseSystem> systems = DataBaseManager.instance.getSystems(0, null); //all systems type giant
            DataBaseStation randomStation = DataBaseManager.instance.getRandomStation(
                    systems,
                    s.getPosition(),
                    SpaceStation.SpaceStationType.PIRATE,
                    rand.nextLong()
            );

            MissionTask killStation = new MissionTaskClearSector(this,"kill","kill",randomStation.getPosition(), SimpleTransformableSendableObject.EntityType.SPACE_STATION,false);
            tasks[1] = killStation;
            killStation.setPreconditions(new int[]{0});
            setMissionTasks(tasks);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public DataBaseStation getTarget() {
        return target;
    }

    public void setTarget(DataBaseStation target) {
        this.target = target;
    }
}
