package me.iron.mGine.mod.missions.wrappers;

import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2021
 * TIME: 21:09
 */
public class DataBaseStation implements Serializable {
    Vector3i position;
    String UID;
    String name;
    int factionID;
    int entityType;
    public DataBaseStation(String UID, String name, Vector3i position, int factionID, int entityType) {
        this.factionID = factionID;
        this.name = name;
        this.position = position;
        this.UID= UID;
        this.entityType = entityType;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(Vector3i position) {
        this.position = position;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFactionID() {
        return factionID;
    }

    public void setFactionID(int factionID) {
        this.factionID = factionID;
    }

    @Override
    public String toString() {
        return "DataBaseStation{" +
                "position=" + position +
                ", UID='" + UID + '\'' +
                ", name='" + name + '\'' +
                ", factionID=" + factionID +
                '}';
    }
}
