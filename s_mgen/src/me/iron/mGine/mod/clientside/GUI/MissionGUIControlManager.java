package me.iron.mGine.mod.clientside.GUI;

import api.DebugFile;
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
import org.schema.schine.graphicsengine.core.GLFrame;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:00
 * 100% clientside
 */
public class MissionGUIControlManager extends GUIControlManager {
    private static int created = 0;
    public static GUIMenuPanel p;
    public static MissionGUIControlManager instance;

    public static int windowWidth = (int) (GLFrame.getWidth()*0.5);
    public static int windowHeight = (int) (GLFrame.getHeight()*0.5);
    public MissionGUIControlManager(GameClientState state) {
        super(state);
        instance = this;
        initListener();
    }

    @Override
    public GUIMenuPanel createMenuPanel() { //gets called twice
        created ++;
        int width = GLFrame.getWidth();
        int heigt = GLFrame.getHeight();
        p = new MissionMenuPanel(getState(),"menu panel",width/2, heigt/2);
        p.onInit();
        p.recreateTabs();
        return p;
    }

    private void initListener() {
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
