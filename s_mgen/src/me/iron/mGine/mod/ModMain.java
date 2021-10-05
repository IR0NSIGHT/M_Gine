package me.iron.mGine.mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.clientside.map.SpriteList;
import me.iron.mGine.mod.debug.DebugUI;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.missions.DataBaseManager;
import org.schema.schine.resource.ResourceLoader;

import java.sql.SQLException;

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
        try {
            new DataBaseManager();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void onClientCreated(ClientInitializeEvent clientInitializeEvent) {
        new MissionClient();
        //DebugUI.localInit();


        super.onClientCreated(clientInitializeEvent);
    }

    @Override
    public void onResourceLoad(ResourceLoader resourceLoader) {
        super.onResourceLoad(resourceLoader);
        SpriteList.loadSprites();
    }
}
