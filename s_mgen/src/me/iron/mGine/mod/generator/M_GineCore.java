package me.iron.mGine.mod.generator;



import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.missions.DataBaseManager;
import me.iron.mGine.mod.network.PacketMissionSynch;
import org.schema.common.util.linAlg.Vector3i;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:10
 */
public class M_GineCore {
    public static M_GineCore instance;
    private static int nextID = 0; //load mechanism from persistent

    public static int getNextID() {
        return nextID++;
    }
    public M_GineCore() {
        instance = this;
        updateLoop(1);
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
            m.notifyObservers(m);
        }
        if (missions.size() != 0) {
            synchMissions();
        }
    }

    private void synchMissions() {
        new PacketMissionSynch(getMissions()).sendToAll();
    }

    private HashSet<Mission> missions = new HashSet<>();

    public HashSet<Mission> getMissions() {
        return missions;
    }

    public static Mission generateMission(long seed, Vector3i center) {
        Random rand = new Random(seed);
        //select random type
        MissionType type = MissionType.getByIdx(rand.nextInt());
        //generate that type
        Mission m = type.generate(rand, seed, center);
        M_GineCore.instance.missions.add(m);
        return m;
    }
}
