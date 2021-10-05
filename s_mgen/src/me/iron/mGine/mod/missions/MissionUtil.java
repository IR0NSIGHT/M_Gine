package me.iron.mGine.mod.missions;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.iron.mGine.mod.generator.Mission;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
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

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 13.09.2021
 * TIME: 21:20
 */
public class MissionUtil {
    public static void giveMoney(int amount, PlayerState player) {
        player.setCredits(player.getCredits()+amount);
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
        return formatter.format(amount)+"c";
    }

    public static void notifyParty(Collection<PlayerState> players, String message, int type) {
        for (PlayerState p: players) {
            p.sendServerMessage(Lng.astr(message),type);
        }
    }

    public static void main(String[] args) {

    }

}
