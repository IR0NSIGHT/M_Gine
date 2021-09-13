package me.iron.mGine.mod.GUI;

import api.utils.gui.GUIMenuPanel;

import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collection;


/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:01
 */
public class MissionMenuPanel extends GUIMenuPanel {
    GUIAncor triggerElement;
    GUIContentPane triggers;
    private GUITextOverlay triggerText;

    static int width = 400;
    static int height = 400;
    FontLibrary.FontSize fontSize =  FontLibrary.FontSize.MEDIUM;

    public MissionMenuPanel(InputState state) {
        super(state,"MyMenuPanel",width,height);
    }

    @Override
    public void recreateTabs() {
        guiWindow.clearTabs();
        ArrayList<Mission> active = new ArrayList<>();
        ArrayList<Mission> available = new ArrayList<>();
        ArrayList<Mission> finished = new ArrayList<>();
        for (Mission m: M_GineCore.instance.getMissions()) {
            switch (m.getState()) {
                case IN_PROGRESS:
                    active.add(m);
                case OPEN:
                    available.add(m);
                default:
                    finished.add(m);
            }
        }
        //active missions
        addMissionTab(active,"ACTIVE");
        addMissionTab(available,"AVAILABLE");
        addMissionTab(finished,"FINISHED");
        //available missions

        //finished missions
    }

    private void addMissionTab(Collection<Mission> missionCollection, String tabName) {
        GUIContentPane tab = guiWindow.addTab(tabName);
        triggers = tab;
        tab.setTextBoxHeightLast(500);

        //background colored instantiation, so it can be used as a parent for auto resizing
        final GUIColoredRectangle background = new GUIColoredRectangle(getState(),height,width, tab.getContent(0),new Vector4f(0.3f,0.3f,0.3f,1));
        background.onInit();
        tab.getContent(0).attach(background);
        final GUIScrollabeElementList list = new GUIScrollabeElementList(width,height,tab,getState());
        list.onInit();
        list.setMissions(missionCollection);
        list.setCallBackActivation(new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {

            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        list.dependent = background;
        background.attach(list);

        //static accessor
        triggerElement = background;

    }

}
