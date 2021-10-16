package me.iron.mGine.mod.missions;

import api.config.BlockConfig;
import com.sun.xml.internal.bind.v2.model.core.ElementInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import me.iron.mGine.mod.missions.wrappers.DataBaseSystem;
import org.hsqldb.server.Server;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.FTLConnection;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.network.server.ServerMessage;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 13.09.2021
 * TIME: 21:20
 */
public class MissionUtil {
    private static int creditsPerDistance = 1; //credits payed per meter
    private static float dangerModifier = 1.2f;
    private static float reputationModifier = 1.2f;

    public static void giveMoney(int amount, PlayerState player) {
        player.setCredits(player.getCredits()+amount);
        player.sendServerMessage(Lng.astr( formatMoney(amount)+" credits received."), ServerMessage.MESSAGE_TYPE_INFO);
    }

    public static void giveMoney(int amount,String playerName) {
       PlayerState p = GameServerState.instance.getPlayerStatesByName().get(playerName);
       if (p != null) {
           giveMoney(amount,p);
       }
    }

    public static String formatMoney(int credits) {

        double amount = Double.parseDouble(""+credits);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String out = formatter.format(amount);
        return formatter.format(amount);
    }

    public static void notifyParty(Collection<PlayerState> players, String message, int type) {
        for (PlayerState p: players) {
            p.sendServerMessage(Lng.astr(message),type);
        }
    }

    /**
     * format into hh:mm:ss
     * @param time in seconds
     * @return
     */
    public static String formatTime(long time) {
        return (String.format("%02d:%02d:%02d",(time % (60*60*60))/(60*60),(time % 3600) / 60,time % 60));
    }

    public static String getRemainingTime(Mission m) {
        int r;
        if (m.getState().equals(MissionState.SUCCESS)||m.getState().equals(MissionState.FAILED)) {
            return "00:00:00";
        } else if (m.getStartTime() > 0) {
            r = (int) (m.getDuration() - (System.currentTimeMillis()-m.getStartTime())/1000);
        } else {
            r = m.getDuration();
        }
        return formatTime(Math.max(0,r));
    }

    public static void main(String[] args) {
        System.out.println(formatTime(2135987125397124587L));
    }

    /**
     * calculate distance between a and b.
     * @param a
     * @param b
     * @return
     */
    public static float getDistance(Vector3i a, Vector3i b) {
        Vector3i d = new Vector3i(a);
        d.sub(b);
        return d.length();
    }

    /**
     * returns (existing) random station in a random system of given NPC.
     * @param factionID factionid
     * @return
     */
    public static DataBaseStation getRandomNPCStationByFaction(int factionID, @Nullable Vector3i blackList, Random random) {
        try {
            ArrayList<DataBaseSystem> systems = DataBaseManager.instance.getSystems(factionID);
            Collections.shuffle(systems);
            DataBaseStation station = null;
            Iterator<DataBaseSystem> iterator = systems.iterator();
            while (station == null && iterator.hasNext()) {
                //loop all systems to find a station
                DataBaseSystem system = iterator.next();
                Vector3i start = new Vector3i(system.getPos()); start.scale(16);
                Vector3i end = new Vector3i(start); end.add(15,15,15);
                ArrayList<DataBaseStation> stations = DataBaseManager.instance.getEntitiesNear(start,end, SimpleTransformableSendableObject.EntityType.SPACE_STATION,factionID,blackList);
                if (stations.size()== 0)
                    continue;

                station = stations.get(random.nextInt(stations.size()));
            }
            return station;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public static ElementInformation getRandomElement(long seed) {
        Random r = new Random(seed);

        //ArrayList<ElementInformation> eis = BlockConfig.getElements();
        //return eis.get(r.nextInt(eis.size()));
        int i = 0;
        ElementInformation ei = null;
        while (i < 50) {
            i++;
            ei = ElementKeyMap.getInformationKeyMap().get((short)r.nextInt(256));
            if (ei != null && ei.isShoppable() && !ei.isDeprecated()) //TODO threw a nullpoint once?!
                break;
        }

        return ei;
    }

    /**
     * will calculate a reward amount based on distance, danger, reputation and random.
     * @param distanceTravelled distance travelled overall
     * @param dangerLevel danger level 1..10
     * @param reputation reputation 1..10
     * @param seed seed for random factor
     * @return
     */
    public static int calculateReward(int distanceTravelled, int dangerLevel, int reputation, long seed) {
        creditsPerDistance = 1;
        Random rand = new Random(seed);
        return (int) ((distanceTravelled * creditsPerDistance)*Math.pow(dangerModifier,dangerLevel)*Math.pow(reputationModifier,reputation)*(0.5f+0.5f*rand.nextFloat()));
    }
}
