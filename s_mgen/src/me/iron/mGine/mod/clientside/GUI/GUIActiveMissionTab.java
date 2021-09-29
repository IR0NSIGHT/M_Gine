package me.iron.mGine.mod.clientside.GUI;

import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.09.2021
 * TIME: 13:39
 */
public class GUIActiveMissionTab extends GUIScrollablePanel {
    public static GUIActiveMissionTab instance;
    public GUIActiveMissionTab(float width, float height, GUIElement dependent, InputState state) {
        super(width, height, dependent, state);
        instance = this;
    }

    @Override
    public void onInit() {

        GUIAncor testPanel = new GUIAncor(getState(),getWidth(),90);
        testPanel.onInit();

        //text field that shows mission description
        GUITextOverlay text = new GUITextOverlay((int) (getWidth()/2),45,getState());
        text.onInit();
        text.getText().add(new Object(){
            @Override
            public String toString() {
                if (activeMission == null)
                    return "no active mission.";
                return activeMission.getDescription();
            }
        });

        //button to abort/accept mission. auto updates text, calls back to onToggleButtonClicked.
        GUITextButton button = new GUITextButton(getState(), (int) (getWidth() / 2), 90, new Object(){
            @Override
            public String toString() {
                if (activeMission == null)
                    return "";
                return getButtonText(activeMission.getState());
            }
        }, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse())
                    onToggleButtonClicked();
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        button.onInit();


        text.setPos(new Vector3f(getWidth()/2,0,0));
        button.setPos(new Vector3f(0,0,0));
        testPanel.attach(text);
        testPanel.attach(button);
        setContent(testPanel);


        //make textfield placeholder for later
        missionText = text;
        buttonToggleActivation = button;

        super.onInit();
    }

    private GUITextOverlay missionText;
    private GUITextButton buttonToggleActivation;
    private Mission activeMission;

    //will try to get the selected mission from missionclient.
    public void getSelectedMissionFromClient() {
        try {
            if (MissionClient.instance == null)
                return;

            if (missionText == null || buttonToggleActivation == null)
                return;

            //overwrite the text of placeholder
            if (MissionClient.instance.getSelectedMission() != null) {
                Mission m = MissionClient.instance.getSelectedMission();
                activeMission = m;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onToggleButtonClicked() {
        Mission m = activeMission;
        if (activeMission == null)
            return;

        switch (m.getState()) {
            case IN_PROGRESS: {
                //abort
                m.setState(MissionState.ABORTED);
                return;
            }
            case OPEN: {
                //accept
                m.start(System.currentTimeMillis());
                return;
            }
        }
    }

    private String getButtonText(MissionState state) {
        switch (state) {
            case OPEN: return "accept mission";
            case IN_PROGRESS: return "abort mission";
            default: return "no action possible";
        }
    }
}
