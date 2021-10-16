package me.iron.mGine.mod.network;

import api.ModPlayground;
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
    private ArrayList<UUID> changedQueue = new ArrayList<>();

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
                if (mp == null)
                    return;

                mp.flagUpdateLocal(); //update visibility for open quests.
            }
        },ModMain.instance);
    }

    /**
     * global update clock ticks, update and synch all players.
     */
    public void onGlobalUpdate() {
        for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
            if (getPlayerByName(p.getName())==null) {
                ModPlayground.broadcastMessage("created mp for " + p.getName());
                addPlayer(p.getName());
            }
        }

        for (MissionPlayer mp: playersByName.values()) {
            mp.onGlobalUpdate(changedQueue);
        }
        changedQueue.clear();
    }

    /**
     * flags this mission to be checked by the players.
     * @param uuid
     */
    public void onMissionChanged(UUID uuid) {
        changedQueue.add(uuid);
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
            p.flagUpdateAll();
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
        mp.flagUpdateAll();
    }

    public void setAdminSeeAll(String playerName, boolean seeAll) {
        MissionPlayer mp = getPlayerByName(playerName);
        mp.setShowAll(seeAll);
    }

}
