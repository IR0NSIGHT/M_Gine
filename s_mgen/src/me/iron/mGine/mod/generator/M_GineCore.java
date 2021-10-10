package me.iron.mGine.mod.generator;



import api.DebugFile;
import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.missions.DataBaseManager;
import me.iron.mGine.mod.network.MissionNetworkController;
import me.iron.mGine.mod.network.PacketMissionSynch;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:10
 */
public class M_GineCore implements Serializable { //TODO make serializable
    public static M_GineCore instance;
    private static int nextID = 0; //load mechanism from persistent

    private HashSet<Mission> missions = new HashSet<>();
    private transient HashMap<UUID,Mission> uuidMissionHashMap = new HashMap<>();

    public M_GineCore() {
        instance = this;
        updateLoop(1);
        new MissionNetworkController();
    }

    public void updateLoop(final int intervallSeconds) {
        new StarRunnable() {
            long last = 0;
            @Override
            public void run() {

                if (last + intervallSeconds*1000<System.currentTimeMillis()) {
                    last = System.currentTimeMillis();
                    updateAll();
                }
            }
        }.runTimer(ModMain.instance,1);
    }

    private void updateAll() {
        for(Mission m: missions) {
            m.update(System.currentTimeMillis());
        }
        if (missions.size() != 0) {
            MissionNetworkController.instance.synchAllPlayers();
        }
    }

    public void addMission(Mission m) {
        missions.add(m);
        uuidMissionHashMap.put(m.uuid,m);
        onMissionUpdate(m);
    }

    public void removeMission(Mission m) {
        missions.remove(m);
        uuidMissionHashMap.remove(m.uuid);
        onMissionUpdate(m);
    }


    public HashSet<Mission> getMissions() {
        return missions;
    }

    public void clearMissions() {
        missions.clear();
        uuidMissionHashMap.clear();
        PacketMissionSynch clearAll = new PacketMissionSynch(new ArrayList<Mission>());
        clearAll.setClearClient(true);
        clearAll.sendToAll();
    }

    /**
     * @param uuid
     * @return mission or null
     */
    public Mission getMissionByUUID(UUID uuid) {
        return uuidMissionHashMap.get(uuid);
    }

    /**
     * runs when a mission is updated.
     */
    public void onMissionUpdate(Mission m) {
        if (m.getState().equals(MissionState.OPEN))
            MissionNetworkController.instance.updateMissionForAll(m);
        //update the player wrappers
        MissionNetworkController.instance.updatePlayers(m.getUuid());
        MissionNetworkController.instance.synchMission(m.getUuid());
    }

//static shit
    public static int getNextID() {
        return nextID++;
    }

    public static Mission generateMission(long seed, Vector3i center) {
        Random rand = new Random(seed);
        //select random type
        MissionType type = MissionType.getByIdx(rand.nextInt());
        //generate that type
        Mission m = type.generate(rand, seed, center);
        M_GineCore.instance.addMission(m);
        return m;
    }
}
