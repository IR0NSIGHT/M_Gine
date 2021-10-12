package me.iron.mGine.mod.generator;



import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.network.MissionNetworkController;
import me.iron.mGine.mod.network.PacketMissionSynch;
import org.schema.game.server.data.GameServerState;

import java.io.Serializable;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:10
 */
public class M_GineCore implements Serializable { //TODO make serializable
    public static M_GineCore instance;
    public int missionsLimit = 3;
    public int garbageCollectorInterval =1000*60*30; //in millis

    private Random rand;
    private HashSet<Mission> missions = new HashSet<>();
    private transient HashMap<UUID,Mission> uuidMissionHashMap = new HashMap<>();

    public M_GineCore() {
        instance = this;
        updateLoop(1);
        rand = new Random();
        new MissionNetworkController();
        new LoreGenerator();
    }

    public void updateLoop(final int intervallSeconds) {
        new StarRunnable() {
            long last = 0;
            @Override
            public void run() {

                if (last + intervallSeconds*1000<System.currentTimeMillis() && GameServerState.instance.getPlayerStatesByName().values().size() != 0) {
                    last = System.currentTimeMillis();
                    updateAll();
                }
            }
        }.runTimer(ModMain.instance,1);
    }

    private void updateAll() {

        this.missionsLimit = 5;
        ArrayList<Mission> removeQueue = new ArrayList<>();
        for(Mission m: missions) {
            m.update(System.currentTimeMillis());
            if (isObsolete(m)) {
                removeQueue.add(m);
            }
        }


        for (Mission m: removeQueue) {
            removeMission(m);
        }


        //add new missions to keep amount always at missionsLimit
        while (missions.size()<missionsLimit) {
            addMission(generateMission(rand.nextLong()));
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
        MissionNetworkController.instance.updateMissionForAll(m);
        //onMissionUpdate(m);
    }

    /**
     * test if a mission should be removed (f.e. wasnt claimed for an hour or so
     * @return
     */
    private boolean isObsolete(Mission m) {
        boolean finished = m.getState().equals(MissionState.SUCCESS)||m.getState().equals(MissionState.FAILED);
        boolean unclaimedForToLong = (m.getState().equals(MissionState.OPEN) && (System.currentTimeMillis()-m.getPublishTime())> garbageCollectorInterval);
        return finished || unclaimedForToLong;
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

    public static Mission generateMission(long seed) {
        Random rand = new Random(seed);

        //select random type
        MissionType type = MissionType.getByIdx(rand.nextInt());

        //generate that type
        Mission m = type.generate(rand, seed);
        return m;
    }
}
