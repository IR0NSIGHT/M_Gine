package me.iron.mGine.mod.debug;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.M_GineCore;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.09.2021
 * TIME: 14:36
 */
public class DebugUI {
    //serverside
    public static void init() {
        //add chat EH
        DebugFile.log("################## ADDING DEBUG UI",ModMain.instance);
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                Random seedSpawn = new Random(420);

                if (!event.isServer())
                    return;
                String txt = event.getText();
                if (txt.contains("new m")) {
                    for (int i = 0; i < 5; i++) {
                        //generate a new mission
                        Mission m = M_GineCore.generateMission(seedSpawn.nextLong());
                        PlayerState p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                        //store centralized
                        m.addPartyMember(p.getName());
                        PlayerState player = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(m.getParty().iterator().next());
                        m.start(System.currentTimeMillis());
                    }
                }
            }
        }, ModMain.instance);
    }
}
