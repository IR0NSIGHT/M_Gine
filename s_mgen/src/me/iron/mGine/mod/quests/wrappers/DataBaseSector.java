package me.iron.mGine.mod.quests.wrappers;

import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 06.10.2021
 * TIME: 20:36
 */
public class DataBaseSector extends DataBaseSystem{
    private int type; //sector type (station, etc)
    public DataBaseSector(Vector3i pos, Long dbID, int ownerFac, int type) {
        super(pos, dbID, ownerFac);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
