package me.iron.mGine.mod.clientside.GUI;

import api.utils.gui.GUIMenuPanel;

import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.HashSet;


/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:01
 */
public class MissionMenuPanel extends GUIMenuPanel {
    public MissionMenuPanel(InputState state,String name, int width, int height) {
        super(state,"MyMenuPanel",width,height);
    }

    @Override
    public void recreateTabs() {
        guiWindow.clearTabs();

        addSelectedMissionTab();
        addMissionTab(MissionClient.instance.active, GUIMissionListTab.ListType.ACTIVE);
        addMissionTab(MissionClient.instance.available, GUIMissionListTab.ListType.OPEN);
        addMissionTab(MissionClient.instance.finished, GUIMissionListTab.ListType.FINISHED);
        MissionClient.instance.update();
    }

    private void addMissionTab(HashSet<Mission> missionCollection, GUIMissionListTab.ListType type) {
        GUIContentPane tab = guiWindow.addTab(type.getName());

        tab.setTextBoxHeightLast(500);

        //background colored instantiation, so it can be used as a parent for auto resizing
        final GUIColoredRectangle background = new GUIColoredRectangle(getState(),MissionGUIControlManager.windowWidth,MissionGUIControlManager.windowHeight, tab.getContent(0),new Vector4f(0.3f,0.3f,0.3f,1));
        background.onInit();
        tab.getContent(0).attach(background);
        final GUIMissionListTab list = new GUIMissionListTab(MissionGUIControlManager.windowWidth,MissionGUIControlManager.windowHeight,tab,getState(), type);
        list.onInit();
        list.setMissions(missionCollection);

        list.dependent = background;
        background.attach(list);

    }

    private void addSelectedMissionTab() {
        GUIContentPane tab = guiWindow.addTab("Selected");
        int tabWidth = (int) tab.getWidth();
        int tabHeight = (int) tab.getHeight();

        //background colored instantiation, so it can be used as a parent for auto resizing
        final GUIColoredRectangle background = new GUIColoredRectangle(getState(),MissionGUIControlManager.windowWidth,MissionGUIControlManager.windowHeight, tab.getContent(0),new Vector4f(0.3f,0.3f,0.3f,1));
        background.onInit();
        tab.getContent(0).attach(background);
        GUISelectedMissionTab selected = new GUISelectedMissionTab(MissionGUIControlManager.windowWidth,MissionGUIControlManager.windowHeight,tab, getState());
        selected.onInit();

        selected.dependent=background;
        background.attach(selected);
    }
}
