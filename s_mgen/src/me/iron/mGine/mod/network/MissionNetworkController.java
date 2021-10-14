package me.iron.mGine.mod.network;

import api.listener.Listener;
import api.listener.events.player.PlayerChangeSectorEvent;
import api.listener.events.player.PlayerJoinWorldEvent;
import api.listener.events.player.PlayerSpawnEvent;
import api.mod.StarLoader;
import api.network.packets.PacketUtil;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 10.10.2021
 * TIME: 16:56
 * handles sending missions to players
 */
public class MissionNetworkController {
    public static MissionNetworkController instance;

    private HashMap<String,MissionPlayer> playersByName = new HashMap<>();
    private ArrayList<UUID> removeQueue = new ArrayList<>();
    public MissionNetworkController() {
        instance = this;
        initMissionPlayers();
        StarLoader.registerListener(PlayerSpawnEvent.class, new Listener<PlayerSpawnEvent>() {
            @Override
            public void onEvent(PlayerSpawnEvent event) {
                addPlayer(event.getPlayer().getName());
            }

        }, ModMain.instance);

        StarLoader.registerListener(PlayerChangeSectorEvent.class, new Listener<PlayerChangeSectorEvent>() {
            @Override
            public void onEvent(PlayerChangeSectorEvent playerChangeSectorEvent) {
                String player =playerChangeSectorEvent.getPlayerState().getName();
                MissionPlayer mp = getPlayerByName(player);
                mp.updateMissions();
                mp.synchPlayer();
            }
        },ModMain.instance);
    }

    private void initMissionPlayers() {
        HashSet<String> players = new HashSet<>();
        for (Mission m: M_GineCore.instance.getMissions()) {
            players.addAll(m.getParty());
        }
        for (String player: players) {
            MissionPlayer p;
            if (playersByName.containsKey(player)) {
                p = playersByName.get(player);
            } else {
                p = new MissionPlayer(player);
            }
            p.updateMissions();
        }
    }

    public void updatePlayers(UUID uuid) {
        Mission m = M_GineCore.instance.getMissionByUUID(uuid);
        if (m == null)
            return;
        for (String player: m.getParty()) {
            updatePlayer(player,m);
        }
    }

    /**
     * will force every player to check if this mission is relevant for him. use for OPEN missions.
     */
    public void updateMissionForAll(Mission mission) {
        for (MissionPlayer p: playersByName.values()) {
            p.updateMission(mission);
        }
    }

    public void updatePlayer(String name) {
        MissionPlayer p = getPlayerByName(name);
        if (p == null)
            return;
        p.updateMissions();
    }

    public void updatePlayer(String name, Mission mission) {
        MissionPlayer p = getPlayerByName(name);
        if (p == null)
            return;
        p.updateMission(mission);
    }

    /**
     * synch all players server>>client
     */
    public void synchAllPlayers() {
        for (PlayerState player: GameServerState.instance.getPlayerStatesByName().values()) {
            synchPlayer(player);
        }
    }

    /**
     * synch this player server>>client
     * @param player
     */
    public void synchPlayer(PlayerState player) {
        MissionPlayer p = playersByName.get(player.getName());
        if (p != null)
            p.synchPlayer();
    }

    public void synchMission(UUID uuid) {
        Mission m = M_GineCore.instance.getMissionByUUID(uuid);

        if (m != null) {
            //synch open missions to every player.
            if (m.getState().equals(MissionState.OPEN)) {
                for (MissionPlayer p: playersByName.values()) {
                    p.synchPlayer(uuid);
                }
            } else {
                PacketMissionSynch packetMissionSynch = new PacketMissionSynch(Collections.singletonList(m));
                for (PlayerState p: m.getActiveParty()) {
                    PacketUtil.sendPacket(p,packetMissionSynch);
                }
            }
        }
    }

    public MissionPlayer getPlayerByName(String name) {
        return playersByName.get(name);
    }

    public void addPlayer(String playerName) {
        if (getPlayerByName(playerName)!=null)
            return;

        MissionPlayer mp = new MissionPlayer(playerName);
        playersByName.put(playerName, mp);
        mp.updateMissions();
        mp.synchPlayer();
    }

    public void setAdminSeeAll(String playerName, boolean seeAll) {
        MissionPlayer mp = getPlayerByName(playerName);
        mp.setShowAll(seeAll);
    }

}
