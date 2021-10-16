package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.generator.LoreGenerator;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.tasks.MissionTaskScanSector;
import me.iron.mGine.mod.missions.wrappers.DataBaseSector;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
* STARMADE MOD
* CREATOR: Max1M
* DATE: 22.09.2021
* TIME: 00:47
*/public class MissionScout extends Mission {
    private ArrayList<Vector3i> sectors;
    public MissionScout(Random rand, long seed, Vector3i center) {
        super(rand,seed);


        //get random npc system
        ArrayList<DataBaseSystem> systems = new ArrayList<>();
        int i = 0;
        while (systems.size() == 0 && i < 10) {
            i++;
            NPCFaction npcFaction = DataBaseManager.instance.getNPCFactions().get(rand.nextInt(DataBaseManager.instance.getNPCFactions().size()));
            int factionID = npcFaction.getIdFaction();
            this.clientFactionID = factionID;
            this.clientFactionName = npcFaction.getName();
            try {
                systems = DataBaseManager.instance.getSystems(factionID);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        Iterator<DataBaseSystem> systemIterator = systems.iterator();
        Collections.shuffle(systems);
        DataBaseSystem sys = null;
        ArrayList<DataBaseSector> sectors = new ArrayList<>();
        //find sectors with ungenerated stations.
        int wantedAmount = 4 + rand.nextInt(4);
        try {
            while ((sectors.size() <= 4) && systemIterator.hasNext()) {
                sys = systemIterator.next();
                StellarSystem stellarSystem = GameServerState.instance.getUniverse().getStellarSystemFromStellarPos(sys.getPos());
                ArrayList<DataBaseSector> secTemp = DataBaseManager.instance.getSectorsWithStations(stellarSystem, SectorInformation.SectorType.SPACE_STATION, null);
                if (secTemp!=null) {
                    sectors.addAll(secTemp);
                }
            }
            while (sectors.size() > wantedAmount) {
                sectors.remove(0);
            }
            createWaypoints(sectors,rand.nextLong());
        } catch ( IOException throwables) {
            throwables.printStackTrace();
        }

        float totalDistance = 0;
        Vector3i pos = new Vector3i(getMissionTasks()[0].getTaskSector());
        for (i = 0; i< getMissionTasks().length; i++) {
            MissionTask t = missionTasks[i];
            pos.sub(t.getTaskSector());
            totalDistance += pos.length();
            pos.set(t.getTaskSector());
        }
        totalDistance *= GameServerState.instance.getSectorSize();

        this.name = "Scout " + sectors.size() +" sectors for " + clientFactionName + " faction.";

        this.duration = (int) (MissionUtil.estimateTimeByDistance(totalDistance,0.75f)); //~2 mins per sector
        this.rewardCredits = MissionUtil.calculateReward(duration,4,1,rand.nextLong());
        this.briefing = LoreGenerator.instance.enemySpottedNearby(clientFactionID,rand.nextLong()) + "\n Scan these sectors.";
    }

    private void createWaypoints(ArrayList<DataBaseSector> sectors, long seed) {
        int amountSectors = sectors.size();
        Random rand = new Random(seed);
        MissionTask[] tasks = new MissionTask[amountSectors];
        for (int i = 0; i < amountSectors; i++) {
            Vector3i sector = new Vector3i(sectors.get(i).getPos());
            if (i == 0) {
                setSector(sector);
            }
            sector.add(rand.nextInt()%2, rand.nextInt()%2, rand.nextInt()%2);
            MissionTaskScanSector scanSector = new MissionTaskScanSector(this,"scan sector","scan sector"+sector.toStringPure(), sector,false);
            tasks[i] = scanSector;
        }
        this.setMissionTasks(tasks);
    }

    @Override
    public String getSuccessText() {
        return "The " + clientFactionName + " faction is thankful for your service. Our systems are a bit safer now, thanks to your effort.";
    }

    @Override
    protected void onSuccess() {
        super.onSuccess();
        for (PlayerState p: getActiveParty()) {
            MissionUtil.giveMoney( rewardCredits/getActiveParty().size(),p);
            String out = "Thank you for scouting out these " + sectors.size() + " sectors.\n Take these " +
                    MissionUtil.formatMoney(rewardCredits / getActiveParty().size()) + " as compensation for your efforts.\nGodspeed pilot!";
            p.sendServerMessage(Lng.astr(out), ServerMessage.MESSAGE_TYPE_DIALOG);
        }
    }
}
