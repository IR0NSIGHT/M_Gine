package me.iron.mGine.mod.debug;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.MissionState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;

import javax.vecmath.Vector4f;
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
                String txt = event.getText();

                if (!event.isServer()) { //CLIENT SIDE
                    if (txt.contains("notify")) {
                        txt = txt.replace("notify ","");
                        for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
                            //    for (int i = 0; i < 6; i++) {
                            p.sendServerMessage(Lng.astr(txt),1);
                            //    }
                        }



                    }
                } else { //SERVER SIDE

                    if (txt.contains("new m")) {
                        for (int i = 0; i < 15; i++) {
                            //generate a new mission
                            Mission m = M_GineCore.generateMission(seedSpawn.nextLong());
                            if (seedSpawn.nextBoolean()) {
                                //make active
                                PlayerState p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                                m.addPartyMember(p.getName());
                                m.start(System.currentTimeMillis());
                            } else if (seedSpawn.nextBoolean()) {
                                //make availalbe
                                m.setState(MissionState.OPEN);
                            } else {
                                //make finished
                                m.setState((seedSpawn.nextBoolean())?MissionState.SUCCESS:MissionState.FAILED);
                            }
                        }
                        return;
                    }
                }


            }
        }, ModMain.instance);
    }
}
