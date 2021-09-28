package me.iron.mGine.mod.generator;

import me.iron.mGine.mod.missions.MissionPatrolSectors;
import me.iron.mGine.mod.missions.MissionScout;
import org.lwjgl.util.vector.Vector;
import org.schema.common.util.linAlg.Vector3i;

import java.util.HashMap;
import java.util.Random;

/**
 * available missiontypes, lists their classes
 */
public enum MissionType {
    //TRANSPORT_GOODS(MissionExampleTransportGoods.class, "transport goods");
    PATROL(MissionPatrolSectors.class,"patrol"),
    SCOUT(MissionScout.class,"scout");
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
    public Mission generate(Random rand, long seed, Vector3i center) {
        switch (this) {
            case PATROL: {
                //System.out.println("transport stuff");
                return new MissionPatrolSectors(rand,seed, center);
            }
            case SCOUT:
                return new MissionScout(rand, seed, center);
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
