package me.iron.mGine.mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import me.iron.mGine.mod.clientside.GUI.MissionGUIControlManager;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.debug.DebugUI;
import me.iron.mGine.mod.generator.M_GineCore;
import org.schema.game.client.data.GameClientState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.09.2021
 * TIME: 14:26
 */
public class ModMain extends StarMod {
    public static ModMain instance;
    @Override
    public void onEnable() {
        instance = this;
        new M_GineCore();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent serverInitializeEvent) {
        super.onServerCreated(serverInitializeEvent);
        DebugUI.init();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent clientInitializeEvent) {
        new MissionClient();
        //DebugUI.localInit();


        super.onClientCreated(clientInitializeEvent);
    }
}
