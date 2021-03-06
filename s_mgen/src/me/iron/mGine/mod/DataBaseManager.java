package me.iron.mGine.mod;

import me.iron.mGine.mod.quests.wrappers.DataBaseSector;
import me.iron.mGine.mod.quests.wrappers.DataBaseSystem;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import me.iron.mGine.mod.quests.wrappers.DataBaseStation;
import org.schema.game.server.data.simulation.npc.NPCFaction;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2021
 * TIME: 18:33
 */
public class DataBaseManager {
    private final Connection connection;
    public static DataBaseManager instance;
    private ArrayList<NPCFaction> npcFactions = new ArrayList<>();
    public DataBaseManager() throws SQLException {
        instance = this;
        String maxNIOSize = "";
        if (FastMath.isPowerOfTwo((Integer) ServerConfig.SQL_NIO_FILE_SIZE.getCurrentState())) {
            maxNIOSize = ";hsqldb.nio_max_size=" + ServerConfig.SQL_NIO_FILE_SIZE.getCurrentState() + ";";
        } else {
            throw new SQLException("server.cfg: SQL_NIO_FILE_SIZE must be power of two (256, 512, 1024,...), but is: " + ServerConfig.SQL_NIO_FILE_SIZE.getCurrentState());
        }
        this.connection = DriverManager.getConnection("jdbc:hsqldb:file:" + DatabaseIndex.getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
        collectNPCFactions();
    }

    /**
     * will return a list of entites matching criteria in range to center sector
     * @param from start sector (must be smaller than to vector!)
     * @param to end sector
     * @param type type to search
     * @return list of found objects as wrappers that contain a name, UID, position, faction and type.
     */
    public ArrayList<DataBaseStation> getEntitiesNear(Vector3i from, Vector3i to,@Nullable SimpleTransformableSendableObject.EntityType type,@Nullable Integer faction,@Nullable Vector3i blackList) throws SQLException {
        Statement s = connection.createStatement();
        //TODO entity type selection.
        String query = "SELECT UID, NAME, X, Y, Z, FACTION, TYPE FROM ENTITIES WHERE X BETWEEN "+from.x+"AND "+to.x+ " AND Y BETWEEN " + from.y +" AND " + to.y + " AND Z BETWEEN " + from.z + " AND " + to.z;
        query += " AND NAME NOT LIKE 'Temporary%'";
        if (blackList != null) {
            query += "AND X != " + blackList.x +" AND Y != " + blackList.y +" AND Z != "+blackList.z;
        }
        if (faction != null) {
            query += " AND FACTION = " + faction;
        }

        if (type != null) {
            query += " AND TYPE = " + type.dbTypeId;
        }
        ResultSet result = s.executeQuery(query);
        ArrayList<DataBaseStation> entities = new ArrayList<>();
        while (result.next()) {
            String UID = DatabaseEntry.getWithFilePrefix(result.getString(1), result.getByte(7));
            DataBaseStation entity = new DataBaseStation(UID,result.getString(2).trim(),new Vector3i(result.getInt(3),result.getInt(4),result.getInt(5)),result.getInt(6), type.dbTypeId);
            entities.add(entity);
        }
        return entities;
    }

    public ArrayList<DataBaseSystem> getSystems(int ownerFaction) throws SQLException {
       return getSystems(ownerFaction,null);
    }

    /**
     * does sql query into DB. must be given at least one parameter, will throw exception otherwise.
     * @param ownerFaction
     * @param systemType
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public ArrayList<DataBaseSystem> getSystems(@Nullable Integer ownerFaction,@Nullable SectorInformation.SectorType systemType) throws SQLException, IllegalArgumentException {
        ArrayList<DataBaseSystem> stellarIDs = new ArrayList<>();
        Statement s = connection.createStatement();
        String queryString = "SELECT ID, X, Y, Z FROM SYSTEMS WHERE ";
        queryString += "X BETWEEN -63 AND 63 AND Y BETWEEN -63 AND 63 AND Z BETWEEN -63 AND 63 AND ";
        if (ownerFaction != null) {
            queryString += ("OWNER_FACTION = " + ownerFaction);
        }
        if (ownerFaction != null && systemType != null) {
            queryString += " AND ";
        }
        if (systemType != null) {
            queryString += ("TYPE = "+systemType.ordinal());
        }
        queryString += ";";
        if (ownerFaction == null && systemType == null) {
            throw new IllegalArgumentException("must give at minimum ownerfaction or systemtype ");
        }
        ResultSet query = s.executeQuery(queryString);
        while (query.next()) {
            long stellarID = query.getLong(1);
            Vector3i pos = new Vector3i(query.getInt(2),query.getInt(3),query.getInt(4));
            DataBaseSystem sys = new DataBaseSystem(pos,stellarID,ownerFaction);
            stellarIDs.add(sys);
        }
        return stellarIDs;
    }

    /**
     * @param stellarID database ID of the stellar system the sector is in
     * @param type SectorType to look for.
     * @return list of wrappers containing the sector with positions and types.
     * @throws SQLException
     */
    public ArrayList<DataBaseSector> getSectors(Long stellarID, SectorInformation.SectorType type) throws SQLException {
        Statement s = connection.createStatement();
        ResultSet result = s.executeQuery("SELECT X, Y ,Z, ID from SECTORS WHERE STELLAR = "+stellarID+ " AND TYPE = " + type.ordinal()+ ";");

        ArrayList<DataBaseSector> sectors = new ArrayList<>();
        while (result.next()) {
            Vector3i pos = new Vector3i(result.getInt(1),result.getInt(2),result.getInt(3));
            sectors.add(new DataBaseSector(pos, result.getLong(4),-1,type.ordinal()));
        }
        return sectors;
    }

    /**
     * gets all sectors with (existing or not yet generated) stations
     * This method has to bruteforce each of the 4k sectors. dont overuse.
     * @param system stellar system to search
     * @param type type to return
     * @return list of wrappers for the found sectors
     */
    public ArrayList<DataBaseSector> getSectorsWithStations(StellarSystem system, SectorInformation.SectorType type, SpaceStation.SpaceStationType stationType) {
        //TODO log results so new searches dont have to bruteforce again
        ArrayList<DataBaseSector> sectors = new ArrayList<>();
        Vector3i sectorPos = new Vector3i();
        for (int z = 0, index = 0; z < VoidSystem.SYSTEM_SIZE; z++) {
            for (int y = 0; y < VoidSystem.SYSTEM_SIZE; y++) {
                for (int x = 0; x < VoidSystem.SYSTEM_SIZE; x++) {
                    SectorInformation.SectorType sectorType = system.getSectorType(index);
                    SpaceStation.SpaceStationType stationType1 = system.getSpaceStationTypeType(index);
                    index++;
                    if (!sectorType.equals(type) || (stationType != null & !stationType1.equals(stationType))) {
                        continue;
                    }
                    sectorPos.set(system.getPos());
                    sectorPos.scale(VoidSystem.SYSTEM_SIZE);
                    sectorPos.add(x, y, z);
                    sectors.add(new DataBaseSector(new Vector3i(sectorPos), (long) 0, system.getOwnerFaction(), type.ordinal()));
                }
            }
        }
        return sectors;
    }

    /**
     * gets an assured existing station of this faction that is not at blacklist position.
     * @param systemsIn stellar systems to search/choose from
     * @param blackList not at this pos
     * @param stationType type of station
     * @param seed seed for random
     * @return database station of a randomly selected station. !!might be empty, only "a station(flag) exists at this pos" is known!!
     * if station exists in DB, its returned.
     */
    public DataBaseStation getRandomStation(ArrayList<DataBaseSystem> systemsIn, @Nullable Vector3i blackList, @Nullable SpaceStation.SpaceStationType stationType, long seed) {
        Random r = new Random(seed);
        ArrayList<DataBaseSystem> systems = new ArrayList<>(systemsIn);
        Collections.shuffle(systems,r);
        Iterator<DataBaseSystem> systemIterator = systems.iterator();
        while (systemIterator.hasNext()) {
            DataBaseSystem system = systemIterator.next();
            ArrayList<DataBaseSector> sectors = getSectorsWithStations(getSystem(system.getPos()), SectorInformation.SectorType.SPACE_STATION,stationType);
            if (sectors.size()!=0) {
                //attempt to get the actual station if it exists in this sector
                Vector3i sectorPos = sectors.get(r.nextInt(sectors.size())).getPos();
                try {
                    ArrayList<DataBaseStation> existingStations = getEntitiesNear(sectorPos,sectorPos, SimpleTransformableSendableObject.EntityType.SPACE_STATION,null,null);
                    if (existingStations.size()>0) {
                        return existingStations.get(r.nextInt(existingStations.size()));
                    }
                } catch (SQLException ex) {

                }
                //return an empty template
                return new DataBaseStation("","",sectorPos,Integer.MIN_VALUE, SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbTypeId);
            }
        }
        return null;
    }

    public static StellarSystem getSystem(Vector3i systemPos) {
        try {
            return GameServerState.instance.getUniverse().getStellarSystemFromStellarPos(systemPos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public ArrayList<NPCFaction> getNPCFactions() {
        return npcFactions;
    }

    private void collectNPCFactions() {
        for(Faction f : GameServerState.instance.getFactionManager().getFactionCollection()) {
            if (f instanceof NPCFaction){
                npcFactions.add(((NPCFaction)f));
            }
        }
    }
 }
