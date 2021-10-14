package me.iron.mGine.mod.network;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
    private ArrayList<UUID> removeList = new ArrayList<>();
    private ArrayList<Vector3i> questMarkers = new ArrayList<>();
    private boolean clearClient; //clear missions from client
    /**
     * @param missions missions to send
     */
    public PacketMissionSynch(Collection<Mission> missions) {
        this.missions.addAll(missions);
        for (Mission m: M_GineCore.instance.getMissions()) {
            if (m.getSector() != null) {
                questMarkers.add(m.getSector());
            }
        }
        //questMarkers.addAll(M_GineCore.instance.getQuestMarkers());
    }

    public void setClearClient(boolean clearClient) {
        this.clearClient = clearClient;
    }

    public void addRemoveList(ArrayList<UUID> removeList) {
        this.removeList.addAll(removeList);
    }

    public PacketMissionSynch() {} //default constructor for starloader

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        try {
            this.clearClient = packetReadBuffer.readBoolean();
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

            int sizeRm = packetReadBuffer.readInt();
            removeList.ensureCapacity(sizeRm);
            for (int i = 0; i < sizeRm; i++) {
                removeList.add(packetReadBuffer.readObject(UUID.class));
            }

            size = packetReadBuffer.readInt();
            for (int i = 0; i < size; i++) {
                questMarkers.add((Vector3i)packetReadBuffer.readObject(Vector3i.class));
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeBoolean(clearClient);
        packetWriteBuffer.writeInt(missions.size());
        for (Mission m: missions) {
            m.writeToBuffer(packetWriteBuffer);
        }

        packetWriteBuffer.writeInt(removeList.size());
        for (UUID uuid: removeList) {
            packetWriteBuffer.writeObject(uuid);
        }

        packetWriteBuffer.writeInt(questMarkers.size());
        for (Vector3i pos: questMarkers) {
            packetWriteBuffer.writeObject(pos);
        }
    }

    @Override
    public void processPacketOnClient() {
        if (this.clearClient) {
            MissionClient.instance.overwriteMissions(missions);
        } else {
            MissionClient.instance.addMissions(missions);
        }
        if (removeList.size()>0) {
            MissionClient.instance.removeMissions(removeList);
        }
        if (questMarkers.size()>0) {
            MissionClient.instance.setOpenQuestMarkers(questMarkers);
        }
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        //TODO send back client GUI interaction here
    }

    /**
     * send to every connected client (with a playerstate)
     */
    public void sendToAll() {
        sendTo(GameServerState.instance.getPlayerStatesByName().values());
    }

    public void sendTo(Collection<PlayerState> players) {
        for (PlayerState p: players) {
            PacketUtil.sendPacket(p,this);
        }
    }
}
