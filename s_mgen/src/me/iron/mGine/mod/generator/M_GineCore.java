package me.iron.mGine.mod.generator;



import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;

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

    public M_GineCore() {
        instance = this;
        updateLoop(2);
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
        }.runTimer(ModMain.instance,5);
    }

    private void updateAll() {
        for(Mission m: missions) {
            m.update(System.currentTimeMillis());
        }
    }
    private HashSet<Mission> missions = new HashSet<>();

    public HashSet<Mission> getMissions() {
        return missions;
    }

    public static Mission generateMission(long seed) {
        Random rand = new Random(seed);
        //select random type
        MissionType type = MissionType.getByIdx(rand.nextInt());
        //generate that type
        Mission m = type.generate(rand, seed);
        M_GineCore.instance.missions.add(m);
        return m;
    }
}
