package me.iron.mGine.mod.network;

import api.network.packets.PacketUtil;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.io.Serializable;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 10.10.2021
 * TIME: 17:07
 * wrapper for a player, containing lists of his missions and stats.
 * eventbased updates.
 */
public class MissionPlayer implements Serializable {
    private transient HashSet<UUID> missions = new HashSet(){
        @Override
        public boolean remove(Object o) {
            if (contains(o))
                removeQueue.add((UUID) o);
            return super.remove(o);
        }
    };
    private final String playerName;
    private ArrayList<UUID> removeQueue = new ArrayList<>();
    public MissionPlayer(String playername) {
        this.playerName = playername;
    }

    public void updateMissions() {
        missions.clear();
        for (Mission m: M_GineCore.instance.getMissions()) {
            updateMission(m);
        }
    }

    public void updateMission(Mission m) {
        PlayerState player = GameServerState.instance.getPlayerStatesByName().get(playerName);
        if (player == null)
            return;

        if (M_GineCore.instance.getMissionByUUID(m.getUuid())==null) {
            missions.remove(m.getUuid());
            return;
        }

        if ( m.isVisibleFor(player) || player.isAdmin()) {
            missions.add(m.getUuid());
        }
    }

    public void synchPlayer() {
        synchPlayer(null);
    }

    public void synchPlayer(UUID uuid) {
        synchPlayer(uuid,false);
    }

    /**
     * force synching with mission, ignoring if player even is related to mission
     * @param uuid
     * @param force
     */
    public void synchPlayer(UUID uuid, boolean force) {
        if (!force && (uuid != null && !missions.contains(uuid)))
            return;

        //is player online?
        PlayerState pState = GameServerState.instance.getPlayerStatesByName().get(playerName);
        if (pState == null)
            return;

        Mission m = M_GineCore.instance.getMissionByUUID(uuid);
        if (uuid != null && m == null) //m doesnt exist -> dont synch.
            return;

        ArrayList<Mission> ids = new ArrayList<>();
        if (uuid == null) {
            for (UUID id: missions) {
                Mission mmm = M_GineCore.instance.getMissionByUUID(id);
                if (mmm != null)
                    ids.add(mmm);
            }
        } else {
            ids = new ArrayList<>(1);
            Mission mmm = M_GineCore.instance.getMissionByUUID(uuid);
            if (mmm != null)
                ids.add(mmm);
        }
        //make a packet with this one mission and send to player
        PacketMissionSynch packet = new PacketMissionSynch(ids);
        packet.addRemoveList(removeQueue);
        removeQueue.clear();
        PacketUtil.sendPacket(pState,packet);
    }
}
