package me.iron.mGine.mod.generator;



import api.ModPlayground;
import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.network.MissionNetworkController;
import org.schema.common.util.linAlg.Vector3i;
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
    public int missionsLimit = 25;
    public int garbageCollectorInterval =1000*60*30; //in millis, after 0.75..1.25 * intervall, an unclaimed mission will be deleted, replaced by a new one

    private Random rand;
    private Random randomGC = new Random();
    private StarRunnable gameLoop;
    private HashSet<Mission> missions = new HashSet<>();
    private transient HashMap<UUID,Mission> uuidMissionHashMap = new HashMap<>();
    private HashSet<Vector3i> questMarkers = new HashSet<>();

    public M_GineCore() {
        instance = this;
        rand = new Random();
        new MissionNetworkController();
        new LoreGenerator();
        updateLoop(1);
    }

    public long lastUpdate;
    public boolean pingPong;
    public void updateLoop(final int intervallSeconds) {
         gameLoop = new StarRunnable() {
            long last = System.currentTimeMillis()+1000*5;
            boolean ping;
            @Override
            public void run() {
                if (!this.equals(gameLoop)) {
                    ModPlayground.broadcastMessage("double gameloop detected, will suicide now.");
                    cancel();
                }
                if (last + intervallSeconds*1000<System.currentTimeMillis() && GameServerState.instance.getPlayerStatesByName().values().size() != 0) {
                    last = System.currentTimeMillis();
                    lastUpdate = last;
                    if (M_GineCore.instance.pingPong) {
                        ModPlayground.broadcastMessage("m_gine core mainloop: "+ (ping?"ping":"pong"));
                        ping = !ping;
                    }
                    updateAll();
                }
            }
        };
        gameLoop.runTimer(ModMain.instance,5);
    }

    private void updateAll() {
        ArrayList<Mission> removeQueue = new ArrayList<>();
        for(Mission m: missions) {
            try {
                m.update(System.currentTimeMillis());
                if (isObsolete(m)) {
                    removeQueue.add(m);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        for (Mission m: removeQueue) {
            try {
                removeMission(m);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        //add new missions to keep amount always at missionsLimit
        while (missions.size()<missionsLimit) {
            try {
                addMission(generateMission(rand.nextLong()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //update and synch players
        try {
            MissionNetworkController.instance.onGlobalUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
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

    /**
     * test if a mission should be removed (f.e. wasnt claimed for an hour or so
     * @return
     */
    private boolean isObsolete(Mission m) {
        randomGC.setSeed(m.getSeed());
        boolean finished = m.getState().equals(MissionState.SUCCESS)||m.getState().equals(MissionState.FAILED);
        boolean unclaimedForToLong = (m.getState().equals(MissionState.OPEN) && (System.currentTimeMillis()-m.getPublishTime())> (0.75+0.5* randomGC.nextFloat())*(garbageCollectorInterval));
        return finished || unclaimedForToLong;
    }

    public HashSet<Mission> getMissions() {
        return missions;
    }

    public void clearMissions() {
        HashSet<Mission> temp = new HashSet<>(missions.size());
        temp.addAll(missions);
        for (Mission m: temp) {
            removeMission(m);
        }
        //synch happens on next update cycle, players will sort out themselves.
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
        MissionNetworkController.instance.onMissionChanged(m.getUuid());
        //remove obsolete questmarkers: mission was claimed or deleted.
        if ((!m.getState().equals(MissionState.OPEN) || !getMissions().contains(m))&&m.getSector()!=null) {
            questMarkers.remove(m.getSector());
        }

        if (m.getState().equals(MissionState.OPEN) && getMissions().contains(m)) {
            if (m.getSector()!=null) {
                questMarkers.add(m.getSector());
            }
        }
    }

    public HashSet<Vector3i> getQuestMarkers() {
        return questMarkers;
    }
    public static Mission generateMission(long seed) {
        Random rand = new Random(seed);

        //select random type
        MissionType type = MissionType.getByIdx(rand.nextInt());

        //generate that type
        Mission m = type.generate(rand, seed);
        return m;
    }

    private class GameLoop extends StarRunnable {
        @Override
        public void run() {

        }
    }
}
