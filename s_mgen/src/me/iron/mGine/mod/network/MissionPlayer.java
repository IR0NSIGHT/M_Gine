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
    private final String playerName;
    private boolean showAll; //admin feature to always see all missions.

    private HashSet<UUID> missions = new HashSet();

    private boolean isFlaggedForSynch; //flagged for synching
    private boolean isFlaggedUpdateAll; //flagged to update all missions: basically reboot this player

    private ArrayList<UUID> removeQueue = new ArrayList<>(); //missions to remove on client
    private ArrayList<UUID> synchQueue = new ArrayList<>(); //missions to add/update on client

    public MissionPlayer(String playername) {
        this.playerName = playername;
    }

    /**
     * global update clock ticks, client updates itself.
     */
    public void onGlobalUpdate(ArrayList<UUID> changedMissions) {
        //apply global changes: change in missions, deletion, creating
        //apply local changes: visibilty of open missions
        if (isFlaggedUpdateAll) {
            forceUpdateAllMissions();
        } else {
            //update from list: changed missions
            for (UUID uuid: changedMissions) {
                updateMission(uuid);
            }


        }

        if (isFlaggedForSynch) {
            synchPlayer();
        }

        isFlaggedUpdateAll = false;
        isFlaggedForSynch = false;
    }

    private void forceUpdateAllMissions() {
        ArrayList<UUID> uuids = new ArrayList<>(missions);
        for (UUID uuid: uuids) {
            updateMission(uuid);
        }
        for (Mission m: M_GineCore.instance.getMissions()) {
            updateMission(m.getUuid());
        }
    }

    /**
     * will update the given mission for the player. mission will be synched on next cylce.
     * @param uuid mission to update and flag for synching.
     */
    private void updateMission(UUID uuid) {
        PlayerState player = GameServerState.instance.getPlayerStatesByName().get(playerName);
        if (player == null)
            return;

        Mission m = M_GineCore.instance.getMissionByUUID(uuid);

        boolean existGl = m!=null;
        boolean canSee = existGl && m.isVisibleFor(player) || (player.isAdmin()&&showAll);
        boolean existLc = missions.contains(uuid);

        //remove if mission isnt listed globally or player cant see it.
        if (existLc &&  (!canSee || !existGl)) {
            //TODO dont tell client to delete finished mission.

            missions.remove(uuid);
            removeQueue.add(uuid);
            flagForSynch();
            return;
        }

        if (canSee && existGl) {
            //add mission
            missions.add(m.getUuid());
            synchQueue.add(uuid);
            flagForSynch();
        }
    }

    private void synchPlayer() {
        synchPlayer(null);
    }

    private void synchPlayer(UUID uuid) {
        synchPlayer(uuid,false);
    }

    /**
     * force synching with mission, ignoring if player even is related to mission
     * @param uuid
     * @param force
     */
    private void synchPlayer(UUID uuid, boolean force) {
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

    public HashSet<UUID> getMissions() {
        return missions;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
        forceUpdateAllMissions();
        synchPlayer();
    }

    public boolean isFlaggedForSynch() {
        return isFlaggedForSynch;
    }

    public void flagForSynch() {
        isFlaggedForSynch = true;
    }

    public boolean isFlaggedUpdateAll() {
        return isFlaggedUpdateAll;
    }

    /**
     * flag to update player-dependent changes: can i still see all missions that i have?
     */
    public void flagUpdateLocal() { //TODO find a better way that doesnt involve bruteforcing all missions.
        flagUpdateAll();
    }

    public void flagUpdateAll() {
        isFlaggedUpdateAll = true;
        flagForSynch();
    }
}
