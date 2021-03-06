package me.iron.mGine.mod.debug;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.GameMapDrawListener;
import api.mod.StarLoader;
import api.network.Packet;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.clientside.map.MapMarker;
import me.iron.mGine.mod.clientside.map.MissionMapDrawer;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.DataBaseManager;
import me.iron.mGine.mod.quests.missions.MissionPatrolSectors;
import me.iron.mGine.mod.network.MissionNetworkController;
import me.iron.mGine.mod.network.MissionPlayer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;

import javax.vecmath.Vector4f;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import me.iron.mGine.mod.quests.wrappers.*;
import org.schema.schine.network.objects.Sendable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.09.2021
 * TIME: 14:36
 */
public class DebugUI {
    public static void main(String[] args) {
        Random random = new Random(420);
        ArrayList<Mission> patrols = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            long seed = random.nextLong();
            random.setSeed(seed);
            patrols.add(new MissionPatrolSectors(random,seed));

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

                if (txt.contains("print_packets")) {
                    Packet.dumpPacketLookup();
                }

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

                if (txt.contains("clear ms")) {
                    M_GineCore.instance.clearMissions();
                    return;
                }

                if (txt.contains("clear money")) {
                    for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
                        p.setCredits(0);
                    }
                    return;
                }

                PlayerState p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                if (p == null)
                    return;

                if (txt.contains("fail")) {
                    return;
                }

                if (txt.contains("seeAll")) {
                    boolean see = false;
                    if (txt.contains("true")) {
                        see = true;
                    }
                    MissionPlayer mp = MissionNetworkController.instance.getPlayerByName(p.getName());
                    if (mp == null)
                        return;
                    mp.setShowAll(see);
                    mp.flagUpdateLocal();
                    mp.flagForSynch();
                    ModPlayground.broadcastMessage("set 'seeAll' to "+see);
                    return;
                }
                if (txt.contains("uid")) {
                    Sendable s =GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(p.getSelectedEntityId());
                    if (s == null || !(s instanceof SegmentController))
                        return;
                    SegmentController sc = (SegmentController) s;
                    ModPlayground.broadcastMessage(((SegmentController) s).getName()+" || "+((SegmentController) s).getUniqueIdentifier());
                    return;
                }
                if (txt.contains("synch")) {
                    MissionPlayer mp = MissionNetworkController.instance.getPlayerByName(p.getName());
                    mp.flagForSynch();
                    if (txt.contains("-a")) {
                        mp.flagUpdateAll();
                    }
                    if (txt.contains("-l")) {
                        mp.flagUpdateLocal();
                    }
                    ModPlayground.broadcastMessage("flagged player " + p.getName() + "for synch");
                }

                if (txt.contains("success")) {
                    MissionPlayer mp = MissionNetworkController.instance.getPlayerByName(p.getName());
                    for (UUID id: mp.getMissions()) {
                        Mission m =M_GineCore.instance.getMissionByUUID(id);

                        if (m == null)
                            continue;

                        if (!m.getState().equals(MissionState.IN_PROGRESS))
                            continue;

                        ModPlayground.broadcastMessage("success'd mission" + m.getName());
                        switch (m.getState()) {
                            case IN_PROGRESS:
                                for (MissionTask t: m.getMissionTasks()) {
                                    t.setCurrentState(MissionState.SUCCESS);
                                }
                                //m.setState(MissionState.SUCCESS);
                        }
                    }
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
        M_GineCore.instance.clearMissions();
        amount = 15;
        for (int i = 0; i < amount; i++) {
            //generate a new mission
            Vector3i playerSector = GameServerState.instance.getPlayerStatesByDbId().values().iterator().next().getCurrentSector();
            Mission m = M_GineCore.generateMission(rand.nextLong());

            if (false) {
                //make active
                PlayerState p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(playerName);
                m.addPartyMember(p.getName());
                m.start(System.currentTimeMillis());

            } else if (true) {
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
            ArrayList<DataBaseStation> ents = dbm.getEntitiesNear(new Vector3i(start,start,start),new Vector3i(end,end,end), SimpleTransformableSendableObject.EntityType.SPACE_STATION, null,null);
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

    private static void printTraderStations() {
        try {
            ArrayList<DataBaseSystem> traderSystems = DataBaseManager.instance.getSystems(-10000000);
            int index = 0;
            if (traderSystems != null) {
                ModPlayground.broadcastMessage("traders have " + traderSystems.size()+ "systems.");
                for (DataBaseSystem system: traderSystems) {
                    //ModPlayground.broadcastMessage("traders own system: " + system.getPos());
                    ArrayList<DataBaseSector> stationSectors = DataBaseManager.instance.getSectorsWithStations(
                            GameServerState.instance.getUniverse().getStellarSystemFromStellarPos(system.getPos()),
                            SectorInformation.SectorType.SPACE_STATION,
                            SpaceStation.SpaceStationType.FACTION);
                    if (stationSectors == null) {
                        return;
                    }
                    for (DataBaseSector s: stationSectors) {
                   //    Sector sector =GameServerState.instance.getUniverse().getSector(s.getPos());
                   //    sector.populate(GameServerState.instance);
                        MapMarker m = new MapMarker(s.getPos(),"traders"+index, MapIcon.WP_COMM,new Vector4f(0,1,0,1));
                        m.setBaseScale(0.1f);
                        m.addToDrawList(true);
                        index ++;
                    }

                }
                ModPlayground.broadcastMessage(index+" stations total.");
                MissionMapDrawer.instance.updateInternalList();
            }
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
    }
}
