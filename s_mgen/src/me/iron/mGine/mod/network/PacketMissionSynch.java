package me.iron.mGine.mod.network;

import api.DebugFile;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 07.10.2021
 * TIME: 20:12
 * package to send missions from server to client.
 * the missions only contain values, and dont do any calculating on their own.
 */
public class PacketMissionSynch extends Packet {
    private ArrayList<Mission> missions = new ArrayList<>();

    /**
     * @param missions missions to send
     */
    public PacketMissionSynch(Collection<Mission> missions) {
        this.missions.addAll(missions);
    }

    public PacketMissionSynch() {} //default constructer for starlaoder
    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        try {
            int size = packetReadBuffer.readInt();
            missions.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                try {
                    Class<?> clazz = Class.forName(packetReadBuffer.readString());
                    Mission m = (Mission) packetReadBuffer.readObject(clazz);
                    m.readFromBuffer(packetReadBuffer);
                    missions.add(m);
                } catch (ClassNotFoundException | ClassCastException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        DebugFile.log("buffer start writing missions ---------------------");
        DebugFile.log("buffer wrote mission list size" + missions.size());
        packetWriteBuffer.writeInt(missions.size());
        for (Mission m: missions) {
            m.writeToBuffer(packetWriteBuffer);
        }
        DebugFile.log("buffer done writing ---------------------------------");
    }

    @Override
    public void processPacketOnClient() {
        MissionClient.instance.updateAllMissions(missions);
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        //TODO send back client GUI interaction here
    }

    /**
     * send to every connected client (with a playerstate)
     */
    public void sendToAll() {
        for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
            PacketUtil.sendPacket(p,this);
        }
    }

}
