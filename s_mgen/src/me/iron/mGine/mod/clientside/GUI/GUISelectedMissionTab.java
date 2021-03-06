package me.iron.mGine.mod.clientside.GUI;

import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.MissionUtil;
import me.iron.mGine.mod.network.PacketInteractMission;
import org.newdawn.slick.UnicodeFont;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector4f;

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
        float width = getWidth(), height = getHeight();
        GUIAncor contentPanel = new GUIAncor(getState(),width,getHeight());
        contentPanel.onInit();

        //width and height stuff for sizing and playing objects
        int textWidth = (int) (width*0.75f);
        int buttonY = 90;
        int buttonWidth = (int) width-textWidth;
        int buttonHeight = buttonY;

        UnicodeFont buttonFont = FontLibrary.getBlenderProHeavy20();
        UnicodeFont textFont = FontLibrary.getBlenderProMedium20();

        //missionTextOverlay field that shows mission description
        GUIColoredRectangle textBackground = new GUIColoredRectangle(getState(), textWidth,height/2,new Vector4f(1,0,0,0f));
        textBackground.onInit();

        GUITextOverlay missionTextOverlay = new GUITextOverlay(textWidth, (int) height/2,textFont,getState());
        missionTextOverlay.onInit();
        missionTextOverlay.getText().add(new Object(){
            @Override
            public String toString() {
                if (activeMission == null)
                    return "no active mission.";
                String out = activeMission.getName()+"\n";
                out += activeMission.getBriefing()+"\n";
                out += MissionUtil.getRemainingTime(activeMission) + "\n";
                out += MissionUtil.formatMoney(activeMission.getRewardCredits())+"\n";
                if (activeMission.getClientFactionID()!=0)
                    out += "Required reputation rank: " + activeMission.getRequiredRank().name();
                return out;
            }
        });
        missionTextOverlay.autoWrapOn = textBackground;
        missionTextOverlay.autoHeight = true;
        missionTextOverlay.setClip(0, (int) textBackground.getHeight()-1); //idk why it needs -1
        //missionTextOverlay.setLimitTextDraw(3);
        textBackground.attach(missionTextOverlay);

        GUIColoredRectangle partyBackground = new GUIColoredRectangle(getState(), textWidth,height,new Vector4f(0,1,0,0f));
        textBackground.onInit();

        GUITextOverlay partyTextOverlay = new GUITextOverlay(textWidth, (int) (height/2), textFont,getState());
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
        partyTextOverlay.autoHeight = true;
        partyTextOverlay.autoWrapOn = partyBackground;
        partyTextOverlay.setClip(0,(int)partyBackground.getHeight()-1);
        partyBackground.attach(partyTextOverlay);

        //acceptAndAbortButton to abort/accept mission. auto updates text, calls back to onToggleButtonClicked.
        GUITextButton acceptAndAbortButton = new GUITextButton(getState(), buttonWidth, buttonHeight, buttonFont, new Object(){
            @Override
            public String toString() {
                if (activeMission == null)
                    return "accept mission";
                return getButtonText(activeMission.getState());
            }
        }, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse() && activeMission != null) {
                    onToggleButtonClicked();
                    MissionClient.instance.update();
                }

            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        GUITextButton inviteToPartyButton = new GUITextButton(getState(), buttonWidth,buttonHeight, buttonFont, new Object() {
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
        GUITextButton removeFromPartyButton = new GUITextButton(getState(), buttonWidth,buttonHeight, buttonFont, new Object() {
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

        GUITextButton delayButton = new GUITextButton(getState(), buttonWidth,buttonHeight, buttonFont, "request delay", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (!mouseEvent.pressedLeftMouse() || activeMission == null)
                    return;
                PacketInteractMission packet = new PacketInteractMission(activeMission.getUuid());
                packet.setDelay(true);
                packet.sendToServer();
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        GUITextButton showTasksButton = new GUITextButton(getState(), buttonWidth, buttonHeight, buttonFont, "show tasks", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse() && activeMission != null) {
                    StringBuilder out = new StringBuilder();
                    out.append("Tasks: \n");
                    for (MissionTask t: activeMission.getMissionTasks()) {
                        out.append(t.getTaskSummary()).append("\n");
                    }
                    GameClientState.instance.getServerMessages().add(new ServerMessage(Lng.astr(out.toString()), ServerMessage.MESSAGE_TYPE_DIALOG));
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        GUITextButton toggleOpenMarkers = new GUITextButton(getState(), buttonWidth, buttonHeight, buttonFont, new Object() {
            @Override
            public String toString() {
                return (MissionClient.instance.isDrawOpenMarkers() ? "hide open mission markers" : "show open mission markers");
            }
        }, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse()) {
                    MissionClient.instance.setDrawOpenMarkers(!MissionClient.instance.isDrawOpenMarkers());
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });

        textBackground.setPos(0,0,0);
        partyBackground.setPos(0,(int) (height/2),0);
        acceptAndAbortButton.setPos(textWidth,0,0);
        delayButton.setPos(textWidth,buttonY,0);
        showTasksButton.setPos(textWidth,buttonY*2,0);

        inviteToPartyButton.setPos(textWidth,height/2,0);
        removeFromPartyButton.setPos(textWidth,height/2+buttonY,0);
        toggleOpenMarkers.setPos(textWidth,height/2+buttonY*2,0);

        contentPanel.attach(textBackground);
        contentPanel.attach(partyBackground);

        contentPanel.attach(acceptAndAbortButton);
        contentPanel.attach(delayButton);
        contentPanel.attach(showTasksButton);

        contentPanel.attach(inviteToPartyButton);
        contentPanel.attach(removeFromPartyButton);
        contentPanel.attach(toggleOpenMarkers);

        setContent(contentPanel);


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

            activeMission = null;
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
