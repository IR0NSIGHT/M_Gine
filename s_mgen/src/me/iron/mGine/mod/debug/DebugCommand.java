package me.iron.mGine.mod.debug;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChangeSectorEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.missions.MissionUtil;
import me.iron.mGine.mod.network.MissionNetworkController;
import me.iron.mGine.mod.network.MissionPlayer;
import org.hsqldb.server.Server;
import org.newdawn.slick.util.pathfinding.navmesh.Link;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.10.2021
 * TIME: 21:23
 */
public class DebugCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "mgine";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"mg"};
    }

    @Override
    public String getDescription() {
        return "mgine debug command:\n" +
                "mg synch (synch all players)\n"+
                "mg info (send info about mgine core)\n" +
                "mp ping (activate pingpong message)\n" +
                "mp restart (attempt restarting gameloop)";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        if (strings.length == 1 && strings[0].equals("synch")) {
            playerState.sendServerMessage(Lng.astr("synching all players"), ServerMessage.MESSAGE_TYPE_DIALOG);
            MissionNetworkController.instance.setFlagUpdateSynchAll();
            return true;
        }
        if (strings.length == 1 && strings[0].equals("info")) {
            StringBuilder out = new StringBuilder("MGINE INFO\n");
            out.append("core has ").append(M_GineCore.instance.getMissions().size()).append(" missions.\n");
            out.append("last core update: ").append(MissionUtil.formatTime(M_GineCore.instance.lastUpdate)).append("\n");
            for (Mission m: M_GineCore.instance.getMissions()) {
                out.append(m.getName()).append("\n");
            }
            Collection<MissionPlayer> mps = MissionNetworkController.instance.getMissionPlayers();
            out.append("network controller has ").append(mps.size()).append(" players: \n");
            for (MissionPlayer mp: mps) {
                out.append(mp.toString()).append("\n");
            }
            playerState.sendServerMessage(Lng.astr(out.toString()),ServerMessage.MESSAGE_TYPE_DIALOG);
            return true;
        }
        if (strings.length == 2 && strings[0].equals("seeAll")) {
            boolean showAll = strings[1].equals("true");
            MissionPlayer p = MissionNetworkController.instance.getPlayerByName(playerState.getName());
            if (p == null) {
                playerState.sendServerMessage(Lng.astr("no mp found for " + playerState.getName()),ServerMessage.MESSAGE_TYPE_DIALOG);
                return true;
            }
            p.setShowAll(showAll);
            p.flagUpdateAll();
            p.flagForSynch();
            return true;
        }
        if (strings.length == 1 && strings[0].equals("ping")) {
            M_GineCore.instance.pingPong = !M_GineCore.instance.pingPong;
            playerState.sendServerMessage(Lng.astr("ping pong now: " + M_GineCore.instance.pingPong), ServerMessage.MESSAGE_TYPE_DIALOG);
            return true;
        }
        if (strings.length == 1 && strings[0].equals("restart")) {
            M_GineCore.instance.updateLoop(1);
            MissionNetworkController.instance.addSectorChangeListener();
            return true;
        }
        if (strings.length == 1 && strings[0].equals("listen")) {
            ArrayList<Listener> listeners = StarLoader.listeners.get(PlayerChangeSectorEvent.class);
            for (Listener l: listeners) {
                StarLoader.fireEvent(new PlayerChangeSectorEvent(playerState,1,2),true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
