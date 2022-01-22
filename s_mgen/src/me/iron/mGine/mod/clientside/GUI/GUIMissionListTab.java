package me.iron.mGine.mod.clientside.GUI;


import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.08.2021
 * TIME: 16:42
 * a list which is scrollable and displays GUI elements, like text / buttons
 */
public class GUIMissionListTab extends GUIScrollablePanel implements DrawerObserver, GUICallback {
    private GUIElementList list;
    private ListType type;
    private boolean updateFlag;
    protected enum ListType {
        ACTIVE("Active"),
        OPEN("Open"),
        FINISHED("Finished");
        private String name;
        ListType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    private final HashSet<Mission> missions = new HashSet<>();
    private boolean lockFlag;

    public static Vector4f selectedColor = new Vector4f(0.35f,0.35f,0.35f,1);
    public static Vector4f unselectedColor = new Vector4f(0.298f,0.298f,0.298f,1);

    public static int blockSize = 40;
    public static int blockWidth = blockSize *20;
    public GUIMissionListTab(float width, float height, GUIElement dependent, InputState inputState, ListType type) {
        super(width, height, dependent, inputState);
        this.type = type;
        blockSize = 40;
        blockWidth = (int) getWidth();
        switch (type) {
            case OPEN:
                MissionClient.instance.guiOpenMissionsList = this;
                break;
            case ACTIVE:
                MissionClient.instance.guiActiveMissionsList = this;
                break;
            case FINISHED:
                MissionClient.instance.guiFinishedMissionsList = this;
                break;
        }
    }
    public void flagForUpdate() {
        updateFlag = true;
    }

    public void setMissions(HashSet<Mission> missions) {
        synchronized (this.missions) {
            this.missions.clear();
            this.missions.addAll(missions);
            try {
                flagForUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void onInit() {
        //create a list
        list = new GUIElementList(getState());
        list.onInit();
        this.setContent(list);

        //add a callback for selection and highlighting
        list.setCallback(this);

        for (int i = 0; i < 100; i++) {
            addTextRow(list,"","select");
        }
        list.height = 10000; //doesnt autoresize, idk what //TODO
        super.onInit();
    }

    private void updateList() {
        synchronized (this.missions) { //missions is accessed by the main(?) thread that updates it and the GUI/GL thread that creates the GUI. gotta restrict access to one at a time.
            try {
                list.clear();
                Mission selected = MissionClient.instance.getSelectedMission();
                for (Mission m: missions) {
                    boolean isSelected = m.equals(selected);
                    int blockHeight = blockSize*(isSelected?3:1);
                    final GUIColoredRectangle background = new GUIColoredRectangle(getState(),blockWidth-blockSize,blockHeight,isSelected?selectedColor:unselectedColor);

                    GUITextOverlay textBox1 = new GUITextOverlay(blockWidth-blockSize,blockHeight, FontLibrary.FontSize.BIG,getState());
                    textBox1.onInit();
                    textBox1.getText().add(m.getName());
                    if (isSelected) {
                        textBox1.getText().add(m.getBriefing());
                    }
                    textBox1.autoHeight = true;
                    textBox1.autoWrapOn = background;
                    background.setHeight(textBox1.getHeight());

                    final Mission fm = m;
                    GUITextButton dropButton = new GUITextButton(getState(), blockSize,blockSize, isSelected?"^":"v", new GUICallback() {
                        @Override
                        public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                            if (mouseEvent.pressedLeftMouse()) {
                                Mission selected = MissionClient.instance.getSelectedMission();
                                if (fm.equals(selected)) {
                                    MissionClient.instance.setSelectedMission(null);
                                } else {
                                    MissionClient.instance.setSelectedMission(fm);
                                }
                            }
                        }

                        @Override
                        public boolean isOccluded() {
                            return false;
                        }
                    });

                    textBox1.setPos(0,0,0);
                    dropButton.setPos(blockWidth-blockSize-blockSize,0,0);
                    background.attach(textBox1);
                    background.attach(dropButton);
                    list.addWithoutUpdate(new GUIListElement(background,getState())); //wrap text in list element, and add to list
                }
                list.updateDim();
            } catch (ConcurrentModificationException|NullPointerException ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    public void draw() {
        super.draw();
        if (updateFlag) {
            updateFlag = false;
            updateList();
        }
    }

    private HashMap<GUITextButton, GUIListElement> BUTTON_TO_LIST = new HashMap<>();
    private HashMap<GUIListElement, Mission> LIST_TO_MISSION = new HashMap<>();
    private void addTextRow(GUIElementList list, String missioninfo, String buttonText) {
        //create a text element
        final GUITextOverlay fullText = new GUITextOverlay(10,10, FontLibrary.FontSize.MEDIUM, getState());
        fullText.onInit();
        fullText.getText().add(missioninfo);
        fullText.autoHeight = true;

        GUITextButton b = new GUITextButton(getState(), (int) this.getWidth(), 30, buttonText,this) ;
        b.onInit();

        GUIListElement listElement = new GUIListElement(fullText,fullText,getState());
        listElement.onInit(); //TODO resize list element?

        list.addWithoutUpdate(listElement);
        list.addWithoutUpdate(new GUIListElement(b,getState()));

        BUTTON_TO_LIST.put(b,listElement);
    }

    @Override
    public void update(DrawerObservable drawerObservable, Object o, Object o1) {
        flagForUpdate();
    }

    @Override
    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
        if (mouseEvent.pressedLeftMouse()) {
            if (guiElement instanceof GUITextButton) {
                GUIListElement listElement = BUTTON_TO_LIST.get(guiElement);
                if (listElement != null) {
                    Mission m = LIST_TO_MISSION.get(listElement);
                    if (m != null) {
                        MissionClient.instance.setSelectedMission(m);
                    }
                }
            }
        }
    }

    @Override
    public boolean isOccluded() {
        return false;
    }

}

