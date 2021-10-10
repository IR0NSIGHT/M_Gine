package me.iron.mGine.mod.network;

import api.network.packets.PacketUtil;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
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
    private transient HashSet<UUID> missions = new HashSet<>();
    private final String playerName;

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
        if (m.getParty().contains(playerName) || m.getState().equals(MissionState.OPEN)) {
            missions.add(m.getUuid());
        } else {
            missions.remove(m.getUuid());
        }
    }

    public void synchPlayer() {
        //is player online?
        PlayerState pState = GameServerState.instance.getPlayerStatesByName().get(playerName);
        if (pState == null)
            return;

        //make a packet with all missions that are relevant to him, send
        ArrayList<Mission> mms = new ArrayList<>(missions.size());
        Iterator<UUID> it = missions.iterator();
        while (it.hasNext()) {
            Mission m = M_GineCore.instance.getMissionByUUID(it.next());
            if (m != null)
                mms.add(m);
        }
        PacketMissionSynch packet = new PacketMissionSynch(mms);
        PacketUtil.sendPacket(pState,packet);
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
        if (!force && !missions.contains(uuid))
            return;
        //is player online?
        PlayerState pState = GameServerState.instance.getPlayerStatesByName().get(playerName);
        if (pState == null)
            return;
        Mission m = M_GineCore.instance.getMissionByUUID(uuid);

        //make a packet with this one mission and send to player
        PacketMissionSynch packet = new PacketMissionSynch(Collections.singletonList(m));
        PacketUtil.sendPacket(pState,packet);
    }
}
