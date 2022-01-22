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
        guiWindow.setResizable(false);
        //int width = MissionGUIControlManager.windowWidth, height = MissionGUIControlManager.windowHeight;
        guiWindow.setWidth(MissionGUIControlManager.windowWidth); guiWindow.setHeight(MissionGUIControlManager.windowHeight);
        //float windowW = getWidth(), windowH = guiWindow.getHeight();
        int width = guiWindow.getInnerWidth(), height = guiWindow.getInnerHeigth();
        addSelectedMissionTab(width,height);
        addMissionTab(MissionClient.instance.active, GUIMissionListTab.ListType.ACTIVE,width,height);
        addMissionTab(MissionClient.instance.available, GUIMissionListTab.ListType.OPEN,width,height);
        addMissionTab(MissionClient.instance.finished, GUIMissionListTab.ListType.FINISHED,width,height);
        MissionClient.instance.update();
    }

    private void addMissionTab(HashSet<Mission> missionCollection, GUIMissionListTab.ListType type, int width, int height) {
        GUIContentPane tab = guiWindow.addTab(type.getName());
        tab.setTextBoxHeightLast(500);

        //background colored instantiation, so it can be used as a parent for auto resizing
        final GUIColoredRectangle background = new GUIColoredRectangle(getState(),width,height, tab.getContent(0),new Vector4f(0.3f,0.3f,0.3f,1));
        background.onInit();
        tab.getContent(0).attach(background);
        final GUIMissionListTab list = new GUIMissionListTab(width,height,tab,getState(), type);
        list.onInit();
        list.setMissions(missionCollection);

        list.dependent = background;
        background.attach(list);

    }

    private void addSelectedMissionTab(int width, int height) {
        GUIContentPane tab = guiWindow.addTab("Selected");

        //background colored instantiation, so it can be used as a parent for auto resizing
        final GUIColoredRectangle background = new GUIColoredRectangle(getState(),50,50, tab.getContent(0),GUIMissionListTab.unselectedColor);
        background.onInit();
        tab.getContent(0).attach(background);
        GUISelectedMissionTab selected = new GUISelectedMissionTab(width,height,tab, getState());
        selected.onInit();

        selected.dependent=background;
        background.attach(selected);
    }
}
