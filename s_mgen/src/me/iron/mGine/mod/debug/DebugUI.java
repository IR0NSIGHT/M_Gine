package me.iron.mGine.mod.debug;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.GameMapDrawListener;
import api.mod.StarLoader;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.missions.DataBaseManager;
import me.iron.mGine.mod.missions.MissionUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import me.iron.mGine.mod.missions.wrappers.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.09.2021
 * TIME: 14:36
 */
public class DebugUI {
    public static void main(String[] args) {
         for (int i = 0; i < 100; i++) {
            String txt = Integer.toHexString ((int)System.currentTimeMillis());
            System.out.println(txt);
         }
    }

    //serverside
    public static void init() {
        //add chat EH
        DebugFile.log("################## ADDING DEBUG UI",ModMain.instance);

        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
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
                    M_GineCore.instance.getMissions().clear();
                    String rest = txt.replace("new m ","");
                    String[] args = rest.split(" ",2);
                    int seed = 420;
                    int amount = 20;

                    try {
                        if (args.length>0)
                            seed = Integer.parseInt(args[0]);
                        if (args.length>1)
                            amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {}
                    generateExampleMissions(seed,event.getMessage().sender,amount);
                    return;
                }

                if (txt.contains("clear money")) {
                    for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
                        p.setCredits(0);
                    }
                }

                if (txt.contains("fail")) {

                    MissionClient.instance.getSelectedTask().setCurrentState(MissionState.FAILED);
                }

                if (txt.contains("sql")) {
                    printEntities();
                }
            }


            }
        }, ModMain.instance);

    }

    public static void localInit() {
        FastListenerCommon.gameMapListeners.add(new GameMapDrawListener() {
            @Override
            public void system_PreDraw(GameMapDrawer gameMapDrawer, Vector3i vector3i, boolean b) {

            }

            @Override
            public void system_PostDraw(GameMapDrawer gameMapDrawer, Vector3i vector3i, boolean b) {

            }

            @Override
            public void galaxy_PreDraw(GameMapDrawer gameMapDrawer) {

            }

            @Override
            public void galaxy_PostDraw(GameMapDrawer gameMapDrawer) {

            }

            @Override
            public void galaxy_DrawLines(GameMapDrawer gameMapDrawer) {
             //   Vector3f start = new Vector3f(0,0,0);
             //   Vector3f end = GameClientState.instance.getPlayer().getCurrentSector().toVector3f();
             //   end.scale(VoidSystem.SYSTEM_SIZE_HALF);
//
             //   Vector4f color = new Vector4f(1,1,1,1);
             //   Vector4f endColor = new Vector4f(color);
             //   float thickness = 1;
             //   DrawUtils.drawFTLLine(start,end,color,endColor);
            }

            @Override
            public void galaxy_DrawSprites(GameMapDrawer gameMapDrawer) {

            }

            @Override
            public void galaxy_DrawQuads(GameMapDrawer gameMapDrawer) {

            }
        });

    }

    private static void generateExampleMissions(int seed, String playerName, int amount) {
        Random rand = new Random(seed);
        M_GineCore.instance.getMissions().clear();
        for (int i = 0; i < amount; i++) {
            //generate a new mission
            Vector3i playerSector = GameClientState.instance.getPlayer().getCurrentSector();
            Mission m = M_GineCore.generateMission(rand.nextLong(),playerSector);

            if (rand.nextBoolean()) {
                //make active
                PlayerState p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(playerName);
                m.addPartyMember(p.getName());
                m.start(System.currentTimeMillis());

            } else if (rand.nextBoolean()) {
                //make availalbe
                m.setState(MissionState.OPEN);
            } else {
                //make finished
                m.setState((rand.nextBoolean())?MissionState.SUCCESS:MissionState.FAILED);
            }
        }
    }

    private static void printEntities() {
        try {
            DataBaseManager dbm = new DataBaseManager();
            int start = -100;
            int end = 100;
            ArrayList<DataBaseStation> ents = dbm.getEntitiesNear(new Vector3i(start,start,start),new Vector3i(end,end,end), SimpleTransformableSendableObject.EntityType.SPACE_STATION, null);
            StringBuilder out = new StringBuilder();
            int rows = 0;
            for (DataBaseStation ent: ents) {
                out.append(ent).append("\n");
                rows ++;
                if (rows > 10) {
                    ModPlayground.broadcastMessage(out.toString());
                    out = new StringBuilder();
                }
            }
            ModPlayground.broadcastMessage(out.toString());
            DebugFile.log(out.toString());
        } catch (SQLException throwables) {
            ModPlayground.broadcastMessage("failed");
            throwables.printStackTrace();
        }
    }
}
