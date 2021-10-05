package me.iron.mGine.mod.missions;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.ServerConfig;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import javax.annotation.Nullable;
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
        String query = "SELECT UID, NAME, X, Y, Z, FACTION FROM ENTITIES WHERE X >= "+from.x+"AND X <= "+to.x+ " AND Y >= " + from.y +" AND Y <= " + to.y + " AND Z >= " + from.z + " AND Y <= " + to.y;
        if (faction != null) {
            query += " AND FACTION = " + faction;
        }

        if (type != null) {
            query += " AND TYPE = " + type.dbTypeId;
        }
        ResultSet result = s.executeQuery(query);
        ArrayList<DataBaseStation> entities = new ArrayList<>();
        while (result.next()) {
            DataBaseStation entity = new DataBaseStation(result.getString(1),result.getString(2),new Vector3i(result.getInt(3),result.getInt(4),result.getInt(5)),result.getInt(6), type.dbTypeId);
            entities.add(entity);
        }
        return entities;
    }
 }
