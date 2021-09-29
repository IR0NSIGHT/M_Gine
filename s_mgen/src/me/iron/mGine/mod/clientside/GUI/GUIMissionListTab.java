package me.iron.mGine.mod.clientside.GUI;

import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

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
    private HashSet<Mission> missions = new HashSet<>();

    public GUIMissionListTab(float width, float height, GUIElement dependent, InputState inputState, ListType type) {
        super(width, height, dependent, inputState);
    }

    public void setMissions(HashSet<Mission> missions) {
        for (Mission m: this.missions) {
            m.deleteObserver(this);
        }
        this.missions = missions;
        for (Mission m: missions) {
            m.addObserver(this);
        }
        updateList();
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
        try {
            if (list == null)
                return;
            //TODO button with "navigate to"
            int i = 0;
            for (Mission m: missions) {
                if (i>=list.size())
                    break;
                //overwrite the text of each mission
                GUIListElement listEl = list.get(i);
                if (listEl.getContent() instanceof GUITextOverlay) {
                    GUITextOverlay content = (GUITextOverlay) listEl.getContent();
                    content.getText().clear();
                    String text = (i< missions.size())?m.getDescription():"";
                    content.getText().add(text);
                    LIST_TO_MISSION.put(listEl,m);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        updateList();
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

