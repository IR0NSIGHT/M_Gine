package me.iron.mGine.mod.quests.missions;

import me.iron.mGine.mod.generator.LoreGenerator;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.DataBaseManager;
import me.iron.mGine.mod.MissionUtil;
import me.iron.mGine.mod.quests.tasks.MissionTaskUnloadCargo;
import me.iron.mGine.mod.quests.wrappers.DataBaseStation;
import me.iron.mGine.mod.quests.wrappers.DataBaseSystem;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.network.server.ServerMessage;

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

    private short[] allowedCargo = new short[]{
            452,453,454,456,457,458,459, //crytals
            93,9,102,106,95,103,99,107,96,104,100,108,97,101,105,109,278,279,280,281, //planet ground stuff
            86,87,85,89,90,91,92,86 //terrain stuff
    };

    public MissionTransportCargo(Random rand, long seed) {
        super(rand, seed);
        this.name = "Transport cargo";
        //get station in from sector
        try {
            if (DataBaseManager.instance.getNPCFactions().size() == 0) {
                new NullPointerException("NO NPC FACTIONS DETECTED").printStackTrace();
                return;
            }
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
            cargoID = allowedCargo[rand.nextInt(allowedCargo.length)];
            ElementInformation elementI = ElementKeyMap.getInfo(cargoID);

            int cargoUnits =(int) (cargoVol / elementI.getVolume());
            cargoName = elementI.getName();//TODO get random from existing blocks

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
            to = DataBaseManager.instance.getRandomStation(systems,from.getPosition(), SpaceStation.SpaceStationType.FACTION,seed);
            to.setFactionID(receiverFactionID);

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
            duration = (int) MissionUtil.estimateTimeByDistance(distance.length()*GameServerState.instance.getSectorSize(),0.3f); //time needed to travel distance at max speed plus random bonus of 0..300%
            this.rewardCredits = MissionUtil.calculateReward(duration,1,1,rand.nextLong());

            //set UI stuff
            this.briefing = LoreGenerator.instance.generateTransportBriefing(from,to,cargoName,cargoUnits,rand.nextLong());
            this.name = LoreGenerator.instance.generateTransportName(from,to,cargoName,cargoUnits,rand.nextLong()) + " for " + clientFactionName;
            //TODO requirements: big cargo
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected int canClaimCode(PlayerState p) {
        if (GameServerState.instance.getFactionManager().isEnemy(receiverFactionID,p)) {
            return 4;
        }
        return super.canClaimCode(p);
    }

    @Override
    public String claimError(int errorCode) {
        if (errorCode == 4)
            return "claimant is enemy with receiver faction " + receveiverFactionName;
        return super.claimError(errorCode);
    }

    @Override
    public void update(long time) {
        super.update(time);
    }

    @Override
    protected void onFailure() {
        MissionTaskUnloadCargo load = (MissionTaskUnloadCargo) getMissionTasks()[0];
        MissionTaskUnloadCargo unload = (MissionTaskUnloadCargo) getMissionTasks()[1];
        int loaded = load.getUnitsStart()-load.getUnits();
        int unloaded = unload.getUnitsStart()-unload.getUnits();
        if (loaded>unloaded) {
            failText = "We have noticed that you have not delivered the cargo and instead kept it for yourself. This will have consequences.";
            diplomacyGain[1]*=3;
        }
        super.onFailure();

    }

    @Override
    public void start(long time) {
        super.start(time);
        Faction client = GameServerState.instance.getFactionManager().getFaction(clientFactionID);
        if (client instanceof NPCFaction) {
            MissionUtil.notifyParty(getActiveParty(),"An armed convoi is on the same way. Join them if you like for security.", ServerMessage.MESSAGE_TYPE_DIALOG);
            Vector3i start = missionTasks[0].getTaskSector();
            Vector3i target = missionTasks[1].getTaskSector();
            if (start == null || target == null)
                throw new NullPointerException("start or target sector for transport mission is null");

        }

    }

    @Override
    public String getFailText() {
        return super.getFailText();
    }
}
