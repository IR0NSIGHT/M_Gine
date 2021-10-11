package me.iron.mGine.mod.clientside.GUI;

import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.missions.MissionUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.09.2021
 * TIME: 13:39
 */
public class GUISelectedMissionTab extends GUIScrollablePanel {
    public static GUISelectedMissionTab instance;
    public GUISelectedMissionTab(float width, float height, GUIElement dependent, InputState state) {
        super(width, height, dependent, state);
        instance = this;
    }

    @Override
    public void onInit() {
        MissionClient.instance.selectedMissionTab = this;

        GUIAncor testPanel = new GUIAncor(getState(),getWidth(),getHeight());
        testPanel.onInit();

        //width and height stuff for sizing and playing objects
        int buttonX = (int) getWidth()/2;
        int buttonY = 90;

        //missionTextOverlay field that shows mission description
        GUITextOverlay missionTextOverlay = new GUITextOverlay(buttonX, (int) testPanel.getHeight()/2,FontLibrary.getBlenderProMedium18(),getState());
        missionTextOverlay.onInit();
        missionTextOverlay.getText().add(new Object(){
            @Override
            public String toString() {
                if (activeMission == null)
                    return "no active mission.";
                String out = activeMission.getDescription();
                out += MissionUtil.getRemainingTime(activeMission);
                return out;
            }
        });

        GUITextOverlay partyTextOverlay = new GUITextOverlay(buttonX, (int) (testPanel.getHeight()/2), FontLibrary.getBlenderProMedium18(),getState());
        partyTextOverlay.onInit();
        partyTextOverlay.getText().add(new Object(){
            @Override
            public String toString() {
                if (activeMission == null) {
                    return "Party: \n" + GameClientState.instance.getPlayer().getName();
                } else {
                    StringBuilder out = new StringBuilder();
                    out.append("Party: \n");
                    for (String playername: activeMission.getParty()) {
                        out.append(playername).append("\n");
                    }
                    return out.toString();
                }
            }
        });

        //acceptAndAbortButton to abort/accept mission. auto updates text, calls back to onToggleButtonClicked.
        GUITextButton acceptAndAbortButton = new GUITextButton(getState(), buttonX, (int) (buttonY*0.95f), new Object(){
            @Override
            public String toString() {
                if (activeMission == null)
                    return "accept mission";
                return getButtonText(activeMission.getState());
            }
        }, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse()) {
                    onToggleButtonClicked();
                    MissionClient.instance.update();
                }

            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        GUITextButton inviteToPartyButton = new GUITextButton(getState(), buttonX, (int) (buttonY * 0.95f), new Object() {
            @Override
            public String toString() {
                return "invite player to party";
            }
        }, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                //TODO invite player stuff
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        GUITextButton removeFromPartyButton = new GUITextButton(getState(), buttonX, (int) (buttonY * 0.95f), new Object() {
            @Override
            public String toString() {
                return "remove player from party";
            }
        }, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                //TODO invite player stuff
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        missionTextOverlay.setPos(0,0,0);
        partyTextOverlay.setPos(0,(int) (testPanel.getHeight()/2),0);
        acceptAndAbortButton.setPos(buttonX,0,0);
        inviteToPartyButton.setPos(buttonX,testPanel.getHeight()/2,0);
        removeFromPartyButton.setPos(buttonX,testPanel.getHeight()/2+buttonY,0);

        testPanel.attach(missionTextOverlay);
        testPanel.attach(partyTextOverlay);
        testPanel.attach(acceptAndAbortButton);
        testPanel.attach(inviteToPartyButton);
        testPanel.attach(removeFromPartyButton);
        setContent(testPanel);


        //make textfield placeholder for later
        missionText = missionTextOverlay;
        buttonToggleActivation = acceptAndAbortButton;

        super.onInit();
    }

    private GUITextOverlay missionText;
    private GUITextButton buttonToggleActivation;
    private Mission activeMission;

    //will try to get the selected mission from missionclient and save it as activelist.
    public void update() {
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
                MissionClient.instance.requestAcceptToggleMission(m.getUuid(),false);
                return;
            }
            case OPEN: {
                //accept
                MissionClient.instance.requestAcceptToggleMission(m.getUuid(),true);
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
