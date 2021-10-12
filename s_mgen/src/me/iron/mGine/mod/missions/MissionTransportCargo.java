package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.LoreGenerator;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskDockTo;
import me.iron.mGine.mod.missions.wrappers.DataBaseSector;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.world.*;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2021
 * TIME: 21:05
 */
public class MissionTransportCargo extends Mission {
    private DataBaseStation from;
    private DataBaseStation to;

    private int cargoID; //block id
    private String cargoName; //block name
    private int cargoUnits; //amount to transport

    public MissionTransportCargo(Random rand, long seed) {
        super(rand, seed);
        this.name = "Transport cargo";
        //get station in from sector
        try {
            //get origin
            from = MissionUtil.getRandomNPCStationByFaction(-10000000,rand);
            if (from == null) {
                new NullPointerException().printStackTrace();
                return;
            }

            Vector3i fromSector = from.getPosition();
            //get target
            Vector3i startSearch = new Vector3i(fromSector); startSearch.sub(100,100,100);
            Vector3i endSearch = new Vector3i(fromSector); endSearch.add(100,100,100);
            ArrayList<DataBaseSystem> systems = DataBaseManager.instance.getSystems(-10000000); //traders
            Collections.shuffle(systems,rand);
            for (DataBaseSystem system: systems) {
                StellarSystem stellarSystem = GameServerState.instance.getUniverse().getStellarSystemFromStellarPos(system.getPos());
                ArrayList<DataBaseSector> sectors = DataBaseManager.instance.getSectorsWithStations(stellarSystem, SectorInformation.SectorType.SPACE_STATION, SpaceStation.SpaceStationType.FACTION);
                Collections.shuffle(sectors,rand);
                if (sectors.size()!=0) {
                    to = new DataBaseStation("","trader station",sectors.get(0).getPos(),-10000000, SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbTypeId);
                    break;
                }
            }

            MissionTask pick_up_cargo = new MissionTaskDockTo(this,"pick up cargo","pick up the cargo at station " + from.getName(),false,from.getUID());
            pick_up_cargo.setIcon(MapIcon.WP_PICKUP);
            pick_up_cargo.setTaskSector(from.getPosition());

            MissionTask deliver_cargo = new MissionTaskDockTo(this,"deliver cargo","bring the received cargo to station " + to.getName(),false,"");
            deliver_cargo.setIcon(MapIcon.WP_DROPOFF);
            deliver_cargo.setTaskSector(to.getPosition());

            MissionTask[] tasks = new MissionTask[2];
            tasks[0] = pick_up_cargo;
            tasks[1] = deliver_cargo;

            this.setMissionTasks(tasks);
            this.setSector(from.getPosition());

            deliver_cargo.setPreconditions(new int[]{0});

            Vector3i distance = new Vector3i(fromSector); distance.sub(to.getPosition());
            float difficulty = rand.nextFloat();
            duration = (int) ((int) (distance.length()* 16000 / 450) * (1 + 3* difficulty)); //time needed to travel distance at max speed plus random bonus of 0..300%
            rewardCredits = (int) ((duration/60)*(500000+500000*rand.nextFloat()));

            cargoName = "toilet paper";//TODO get random from existing blocks
            cargoID = -1;
            cargoUnits = 4000;

            //set UI stuff
            this.briefing = LoreGenerator.instance.generateTransportBriefing(from,to,cargoName,cargoUnits,rand.nextLong());
            this.name = LoreGenerator.instance.generateTransportName(from,to,cargoName,cargoUnits,rand.nextLong());
            //TODO requirements: big cargo
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void update(long time) {
        super.update(time);
        tryUpdateTargetStation();
        updateTaskTargets();
    }

    /**
     * at mission generation, the station might not have been generated yet. try and update the missions stored values if station now exists.
     */
    private void tryUpdateTargetStation() {
        //target was an unknown/not yet generated station, fill with UID and name.
        if (to.getUID().equals("") && GameServerState.instance.getUniverse().isSectorLoaded(to.getPosition())) {
            try {
                for (SimpleTransformableSendableObject obj :GameServerState.instance.getUniverse().getSector(to.getPosition()).getEntities()) {
                    if (obj instanceof SpaceStation && obj.getFactionId() == to.getFactionID()) {
                        to.setUID(obj.getUniqueIdentifier());
                        to.setName(obj.getName());
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateTaskTargets() {
        if (!to.getUID().equals("") && ((MissionTaskDockTo)missionTasks[1]).getTargetUID().equals(""))
            ((MissionTaskDockTo)missionTasks[1]).setTargetUID(to.getUID());
    }
}
