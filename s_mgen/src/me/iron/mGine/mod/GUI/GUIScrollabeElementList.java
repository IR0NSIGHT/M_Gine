package me.iron.mGine.mod.GUI;

import api.ModPlayground;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.08.2021
 * TIME: 16:42
 * a list which is scrollable and displays GUI elements, like text / buttons
 */
public class GUIScrollabeElementList extends GUIScrollablePanel implements DrawerObserver {
    private GUIElementList list;
    private GUICallback callbackActivate;

    private Collection<Mission> missions;
    //button params
    int bWidth = 200;
    int bHeight = 20;
    public GUIScrollabeElementList(float width, float height, GUIElement dependent, InputState inputState) {
        super(width, height, dependent, inputState);
    }

    public void setMissions(Collection<Mission> missions) {
        this.missions = missions;
    }
    public void setCallBackActivation (GUICallback callBack) {
        this.callbackActivate = callBack;
    }

    @Override
    public void onInit() {
        //create a list
        list = new GUIElementList(getState());
        list.setScrollPane(this);
        this.setContent(list);
        //debug add a single button
        for (Mission m: M_GineCore.instance.getMissions()) {
            addTextRow(list,m.getDescription());
        }
        list.updateDim();
        //TODO "add code" button
        super.onInit();
    }



    private void addTextRow(GUIElementList list, String missioninfo) {
        //create a text element
        GUITextOverlay textElement = new GUITextOverlay(10,10, FontLibrary.FontSize.MEDIUM, getState());
        textElement.onInit();
        textElement.getText().add(missioninfo);
        textElement.autoHeight = true;
        GUIListElement listElement = new GUIListElement(textElement,getState());
        list.add(listElement);

        final GUITextButton button = new GUITextButton(getState(), bWidth, bHeight, "activate", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (!mouseEvent.pressedLeftMouse())
                    return;
                ModPlayground.broadcastMessage("clicky clicky");
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        button.setColorPalette(GUITextButton.ColorPalette.NEUTRAL);
        button.onInit();
        list.add(new GUIListElement(button,getState()));
    }

    @Override
    public void update(DrawerObservable drawerObservable, Object o, Object o1) {

    }
}
