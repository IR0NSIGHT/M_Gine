package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.generator.LoreGenerator;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskClearSector;
import me.iron.mGine.mod.missions.tasks.MissionTaskMoveTo;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.lwjgl.Sys;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
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
            DataBaseStation clientStation = DataBaseManager.instance.getRandomStation(
                    DataBaseManager.instance.getSystems(client.getIdFaction()),
                    null,
                    SpaceStation.SpaceStationType.FACTION,
                    rand.nextLong()
                    );
            if (clientStation == null || clientStation.getPosition() == null) {
                new NullPointerException().printStackTrace();
                return;
            }
            clientFactionID = client.getIdFaction();
            clientFactionName = client.getName();
            setSector(clientStation.getPosition());
            MissionTask[] tasks = new MissionTask[3];
            MissionTask moveToStart = new MissionTaskMoveTo(this,"speak with the representative of " + clientFactionName,"speak with the representative of " + clientFactionName,clientStation.getPosition(),false);
            tasks[0] = moveToStart;

        //select a pirate station randomly across galaxy
            ArrayList<DataBaseSystem> systems = DataBaseManager.instance.getSystems(0, SectorInformation.SectorType.SUN); //all systems type giant
            DataBaseStation targetStation = DataBaseManager.instance.getRandomStation(
                    systems,
                    clientStation.getPosition(),
                    SpaceStation.SpaceStationType.PIRATE,
                    rand.nextLong()
            );
            String kill ="Destroy the station in " + targetStation.getPosition().toStringPure();
            MissionTask killStation = new MissionTaskClearSector(this,kill,kill,targetStation.getPosition(), SimpleTransformableSendableObject.EntityType.SPACE_STATION,false);
            tasks[1] = killStation;
            killStation.setPreconditions(new int[]{0});

            tasks[2] = new MissionTaskMoveTo(this,"report back","report back to " + clientFactionName,clientStation.getPosition(),false);
            tasks[2].setPreconditions(new int[]{1});
            setMissionTasks(tasks);
            name = "Destroy a pirate station for "+clientFactionName;
            briefing = LoreGenerator.instance.generateAttackStory("Pirate","station",rand.nextLong())+"\n"+LoreGenerator.instance.generateKillEntity(SimpleTransformableSendableObject.EntityType.SPACE_STATION,rand.nextLong());
            Vector3i offset = new Vector3i(targetStation.getPosition());
            offset.sub(clientStation.getPosition());
            System.out.println(offset.toStringPure());
            float distance = offset.length() ;//go there and come back
            float sectorSize = GameServerState.instance.getSectorSize();
            System.out.println(distance + " sectors");
            System.out.println(sectorSize + "sectorSize");
            distance = distance*sectorSize; //go there and come bacl
            float travelTime = MissionUtil.estimateTimeByDistance(distance,0.75f);
            duration = (int) (travelTime + 60*15); //15 mins to fight pirate station
            rewardCredits = MissionUtil.calculateReward(duration,1,1,rand.nextLong());
            rewardCredits += 1000000+rand.nextInt(1000000);
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

    @Override
    public String getSuccessText() {
        return "In the name of " + clientFactionName + ", thank you for your service. Here is your reward.";
    }

    @Override
    public String getFailText() {
        return "Pathetic. We will remember your failure and will choose our candidates for military operations more careful next time.";
    }
}
