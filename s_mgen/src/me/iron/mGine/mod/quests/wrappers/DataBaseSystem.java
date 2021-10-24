package me.iron.mGine.mod.quests.wrappers;

import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 06.10.2021
 * TIME: 20:32
 */
public class DataBaseSystem {
    private Vector3i pos;
    private Long dbID;
    private int ownerFac;

    public DataBaseSystem(Vector3i pos, Long dbID, int ownerFac) {
        this.pos = pos;
        this.dbID = dbID;
        this.ownerFac = ownerFac;
    }

    public Vector3i getPos() {
        return pos;
    }

    public void setPos(Vector3i pos) {
        this.pos = pos;
    }

    public Long getDbID() {
        return dbID;
    }

    public void setDbID(Long dbID) {
        this.dbID = dbID;
    }

    public int getOwnerFac() {
        return ownerFac;
    }

    public void setOwnerFac(int ownerFac) {
        this.ownerFac = ownerFac;
    }
}
