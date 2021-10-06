package me.iron.mGine.mod.missions;

import me.iron.mGine.mod.missions.wrappers.DataBaseSector;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.entry.PlanetEntityMapEntry;
import org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.ServerConfig;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import org.schema.schine.common.language.Lng;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.sql.*;
import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2021
 * TIME: 18:33
 */
public class DataBaseManager {
    private final Connection connection;
    public static DataBaseManager instance;
    public DataBaseManager() throws SQLException {
        instance = this;
        String maxNIOSize = "";
        if (FastMath.isPowerOfTwo((Integer) ServerConfig.SQL_NIO_FILE_SIZE.getCurrentState())) {
            maxNIOSize = ";hsqldb.nio_max_size=" + ServerConfig.SQL_NIO_FILE_SIZE.getCurrentState() + ";";
        } else {
            throw new SQLException("server.cfg: SQL_NIO_FILE_SIZE must be power of two (256, 512, 1024,...), but is: " + ServerConfig.SQL_NIO_FILE_SIZE.getCurrentState());
        }
        this.connection = DriverManager.getConnection("jdbc:hsqldb:file:" + DatabaseIndex.getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
    }

    /**
     * will return a list of entites matching criteria in range to center sector
     * @param from start sector (must be smaller than to vector!)
     * @param to end sector
     * @param type type to search
     * @return list of found objects as wrappers that contain a name, UID, position, faction and type.
     */
    public ArrayList<DataBaseStation> getEntitiesNear(Vector3i from, Vector3i to,@Nullable SimpleTransformableSendableObject.EntityType type,@Nullable Integer faction) throws SQLException {
        Statement s = connection.createStatement();
        //TODO entity type selection.
        String query = "SELECT UID, NAME, X, Y, Z, FACTION, TYPE FROM ENTITIES WHERE X >= "+from.x+"AND X <= "+to.x+ " AND Y >= " + from.y +" AND Y <= " + to.y + " AND Z >= " + from.z + " AND Y <= " + to.y;
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
        ArrayList<DataBaseSystem> stellarIDs = new ArrayList<>();
        Statement s = connection.createStatement();
        ResultSet query = s.executeQuery("SELECT ID, X, Y, Z FROM SYSTEMS WHERE OWNER_FACTION = "+ownerFaction+";");
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
                    if (!sectorType.equals(type) || !stationType1.equals(stationType)) {
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
 }
