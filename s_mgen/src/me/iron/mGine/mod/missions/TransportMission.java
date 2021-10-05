package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskDockTo;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2021
 * TIME: 21:05
 */
public class TransportMission extends Mission {
    private DataBaseStation from;
    private DataBaseStation to;
    public TransportMission(Random rand, long seed, Vector3i fromSector) {
        super(rand, seed);
        //get station in from sector
        try {
            //get origin
            ArrayList<DataBaseStation> fromStations =  DataBaseManager.instance.getEntitiesNear(fromSector,fromSector, SimpleTransformableSendableObject.EntityType.SPACE_STATION,null);
            from = fromStations.get(0);
            //get target
            Vector3i startSearch = new Vector3i(fromSector); startSearch.sub(100,100,100);
            Vector3i endSearch = new Vector3i(fromSector); endSearch.add(100,100,100);
            ArrayList<DataBaseStation> potentialTargets = DataBaseManager.instance.getEntitiesNear(startSearch,endSearch, SimpleTransformableSendableObject.EntityType.SPACE_STATION,null);
            to = potentialTargets.get(rand.nextInt(potentialTargets.size()));

            MissionTask pick_up_cargo = new MissionTaskDockTo(this,"pick up cargo","pick up the cargo at station " + from.getName(),false,from.getUID());
            pick_up_cargo.setIcon(MapIcon.WP_PICKUP);
            pick_up_cargo.setTaskSector(from.getPosition());

            MissionTask deliver_cargo = new MissionTask(this,"deliver cargo","bring the recieved cargo to station " + to.getName());
            deliver_cargo.setIcon(MapIcon.WP_DROPOFF);
            deliver_cargo.setPreconditions(new MissionTask[]{pick_up_cargo});
            deliver_cargo.setTaskSector(to.getPosition());

            this.setMissionTasks(new MissionTask[]{pick_up_cargo,deliver_cargo});

            Vector3i distance = new Vector3i(fromSector); distance.sub(to.getPosition());
            float difficulty = rand.nextFloat();
            duration = (int) ((int) (distance.length()* 16000 / 450) * (1 + 3* difficulty)); //time needed to travel distance at max speed plus random bonus of 0..300%
            rewardCredits = (int) ((duration/60)*(500000+500000*rand.nextFloat()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
