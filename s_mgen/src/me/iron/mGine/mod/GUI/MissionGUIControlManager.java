package me.iron.mGine.mod.GUI;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.input.KeyPressEvent;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.gui.GUIControlManager;
import api.utils.gui.GUIMenuPanel;
import api.utils.gui.ModGUIHandler;
import me.iron.mGine.mod.ModMain;
import org.lwjgl.input.Keyboard;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:00
 * 100% clientside
 */
public class MissionGUIControlManager extends GUIControlManager {
    public static GUIMenuPanel p;
    public static MissionGUIControlManager instance;
    public MissionGUIControlManager(GameClientState state) {
        super(state);
        instance = this;
        initListener();
    }

    @Override
    public GUIMenuPanel createMenuPanel() {
        p = new MissionMenuPanel(getState());
        p.onInit();
        p.recreateTabs();
        return p;
    }

    private void initListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                PlayerState p = GameClientState.instance.getPlayer();
                if (!p.isAdmin())
                    return;
                if (event.getText().contains("!menu")) {
                    for (GUIControlManager manager: ModGUIHandler.getAllModControlManagers()) {
                        manager.setActive(false);
                    }
                    //TODO disable all others
                    setActive(true);
                    if (MissionGUIControlManager.p != null)
                        MissionGUIControlManager.p.recreateTabs();
                    event.setCanceled(true);
                }
            }
        }, ModMain.instance);

        StarLoader.registerListener(KeyPressEvent.class, new Listener<KeyPressEvent>() {
            @Override
            public void onEvent(KeyPressEvent keyPressEvent) {
                //ModPlayground.broadcastMessage("key: " + keyPressEvent.getKey() + keyPressEvent.toString());
                if (Keyboard.isKeyDown(29) && Keyboard.isKeyDown(50)) {
                    for (GUIControlManager manager: ModGUIHandler.getAllModControlManagers()) {
                        manager.setActive(false);
                    }
                    setActive(true);
                    if (MissionGUIControlManager.p != null)
                        MissionGUIControlManager.p.recreateTabs();
                //    ModPlayground.broadcastMessage("CTRL + M = MENU");
                }

            }
        },ModMain.instance);
    }
}
