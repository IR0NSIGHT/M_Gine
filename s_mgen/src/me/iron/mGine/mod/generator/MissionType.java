package me.iron.mGine.mod.generator;

import me.iron.mGine.mod.quests.missions.MissionDestroyStation;
import me.iron.mGine.mod.quests.missions.MissionPatrolSectors;
import me.iron.mGine.mod.quests.missions.MissionScout;
import me.iron.mGine.mod.quests.missions.MissionTransportCargo;
import org.schema.common.util.linAlg.Vector3i;

import java.util.HashMap;
import java.util.Random;

/**
 * available missiontypes, lists their classes
 */
public enum MissionType {
    TRANSPORT_CARGO(MissionTransportCargo.class, "transport cargo"),
    PATROL(MissionPatrolSectors.class,"patrol"),
    SCOUT(MissionScout.class,"scout"),
    DESTROY_STATION(MissionDestroyStation.class,"destroy station");
    /*
    FERRY_PASSENGER(null,"Ferry passengers"),
    DELIVER_GOODS(null,"deliver goods"),
    SCAN_OBJECTS(null,""),
    DESTROY_OBJECTS(null,""),
    PROTECT_OBJECTS(null,"");
    */

    //enum parameters
    Class missionClass;
    String name;

    //constructor
    private MissionType(Class<? extends Mission> missionClass, String name) {
        this.missionClass = missionClass;
        this.name = name;
    }

    //generate a mission from this type
    public Mission generate(Random rand, long seed) {
        switch (this) {
            case PATROL:
                return new MissionPatrolSectors(rand,seed);

            case SCOUT:
                return new MissionScout(rand,seed, new Vector3i(rand.nextInt()%120,rand.nextInt()%120,rand.nextInt()%120));

            case TRANSPORT_CARGO:
                return new MissionTransportCargo(rand, seed);

            case DESTROY_STATION:
                return new MissionDestroyStation(rand, seed);
        }
        return null;


    }

    /**
     * get missiontype by its index, is out-of-bounds safe (abs + modulo)
     *
     * @param idx
     * @return type at that index
     */
    static MissionType getByIdx(int idx) {
        idx = Math.abs(idx) % values().length;
        return values()[idx];
    }

    //maps
    static HashMap<Class, MissionType> BY_CLASS = new HashMap<>();

    //not null safe!
    public static MissionType getByClass(Class mClass) {
        return BY_CLASS.get(mClass);
    }

    //fill map
    static {
        for (MissionType t : MissionType.values()) {
            BY_CLASS.put(t.missionClass, t);
        }
    }


}
