package me.iron.mGine.mod.missions;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.LoreGenerator;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskDockTo;
import me.iron.mGine.mod.missions.tasks.MissionTaskUnloadCargo;
import me.iron.mGine.mod.missions.wrappers.DataBaseSector;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.lib.Collection;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.*;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2021
 * TIME: 21:05
 */
public class MissionTransportCargo extends Mission {
    private DataBaseStation from;
    private DataBaseStation to;

    private short cargoID; //block id
    private String cargoName; //block name
    private int cargoUnits; //amount to transport

    private int receiverFactionID;
    private String receveiverFactionName;

    public MissionTransportCargo(Random rand, long seed) {
        super(rand, seed);
        this.name = "Transport cargo";
        //get station in from sector
        try {
            NPCFaction f = DataBaseManager.instance.getNPCFactions().get(rand.nextInt(DataBaseManager.instance.getNPCFactions().size()));
            clientFactionID = f.getIdFaction();
            clientFactionName = f.getName();
            //get origin
            from = MissionUtil.getRandomNPCStationByFaction(clientFactionID,null,rand);
            if (from == null) {
                new NullPointerException().printStackTrace();
                return;
            }

            int cargoVol = (int) ((10+30*rand.nextFloat())*1000);
            ElementInformation elementI = MissionUtil.getRandomElement(rand.nextLong());
            int cargoUnits =(int) (cargoVol / elementI.getVolume());
            cargoName = elementI.getName();//TODO get random from existing blocks
            cargoID = elementI.getId();

            Vector3i fromSector = from.getPosition();

            //get target
            receiverFactionID = clientFactionID;
            receveiverFactionName = clientFactionName;

            //ship to another (neutral/friendly NPC) faction
            List<NPCFaction> friends = new ArrayList<>(DataBaseManager.instance.getNPCFactions());
            Collections.shuffle(friends);

            Iterator<NPCFaction> factionObjectIterator = friends.iterator();

            while (factionObjectIterator.hasNext()) {
                Faction ff = factionObjectIterator.next();
                //sort out non-npcs and enemies
                if (clientFactionID==ff.getIdFaction() || !ff.isNPC() || (((FactionState)f.getState()).getFactionManager().isEnemy(clientFactionID,ff.getIdFaction())))
                    continue;
                receiverFactionID = ff.getIdFaction();
                receveiverFactionName = ff.getName();
                break;
            }


            Vector3i startSearch = new Vector3i(fromSector); startSearch.sub(1000,1000,1000);
            Vector3i endSearch = new Vector3i(fromSector); endSearch.add(1000,1000,1000);
            ArrayList<DataBaseSystem> systems = DataBaseManager.instance.getSystems(receiverFactionID);
            Collections.shuffle(systems,rand);
            to = DataBaseManager.instance.getExistingRandomStation(systems,from.getPosition(), SpaceStation.SpaceStationType.FACTION,seed);

            //MissionTask pick_up_cargo = new MissionTaskDockTo(this,"pick up cargo","pick up the cargo at station " + from.getName(),false,from.getUID());
            MissionTask pick_up_cargo = new MissionTaskUnloadCargo(this,"Load cargo","load the cargo at station " + from.getName(),from,cargoID,cargoUnits,true,false);

            MissionTask deliver_cargo = new MissionTaskUnloadCargo(this,"Unload cargo","bring the received cargo to station at " + to.getPosition().toStringPure(),to,cargoID,cargoUnits,false,false);

            MissionTask[] tasks = new MissionTask[2];
            tasks[0] = pick_up_cargo;
            tasks[1] = deliver_cargo;

            this.setMissionTasks(tasks);
            this.setSector(from.getPosition());

            deliver_cargo.setPreconditions(new int[]{0});

            Vector3i distance = new Vector3i(fromSector); distance.sub(to.getPosition());
            float difficulty = rand.nextFloat();
            duration = (int) ((int) (distance.length()* 16000 / 450) * (1 + 3* difficulty)); //time needed to travel distance at max speed plus random bonus of 0..300%
            this.rewardCredits = MissionUtil.calculateReward((int) (distance.length()*GameServerState.instance.getSectorSize()),1,1,rand.nextLong());

            //set UI stuff
            this.briefing = LoreGenerator.instance.generateTransportBriefing(from,to,cargoName,cargoUnits,rand.nextLong());
            this.name = LoreGenerator.instance.generateTransportName(from,to,cargoName,cargoUnits,rand.nextLong()) + " for " + clientFactionName;
            //TODO requirements: big cargo
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public boolean canClaim(PlayerState p) {
        boolean isEnemyWithReceiver = GameServerState.instance.getFactionManager().isEnemy(receiverFactionID,p);
        return super.canClaim(p) && !isEnemyWithReceiver;
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
