package me.iron.mGine.mod.GUI;

import api.ModPlayground;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIButton;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.08.2021
 * TIME: 16:42
 * a list which is scrollable and displays GUI elements, like text / buttons
 */
public class GUIScrollabeElementList extends GUIScrollablePanel implements DrawerObserver, GUICallback {
    private GUIElementList list;
    private GUICallback callbackActivate;

    private Collection<Mission> missions = new ArrayList<>();

    public GUIScrollabeElementList(float width, float height, GUIElement dependent, InputState inputState) {
        super(width, height, dependent, inputState);
    }

    public void setMissions(Collection<Mission> missions) {
        this.missions = missions;
    }
    public void setCallBackActivation (GUICallback callBack) {
        this.callbackActivate = callBack;
    }
    GUIListElement selected;
    GUIListElement highlight;
    @Override
    public void onInit() {
        //create a list
        list = new GUIElementList(getState());
        list.onInit();
        this.setContent(list);

        //add a callback for selection and highlighting
        list.setCallback(this);

        rebuildList();
        list.height = 10000; //doesnt autoresize, idk what //TODO
        super.onInit();

    }

    private void rebuildList() {
        try {
           list.clear();
           list.deleteObservers();
           for (Mission m: missions) {
               m.clearObservers();
               m.addObserver(this);
               //todo make small text contain name,reward,time
               addTextRow(list,m.getDescription(),m.getType().name());
           }

        } catch (Exception e) {
            e.printStackTrace();
        }
        list.updateDim();
    }

    private void addTextRow(GUIElementList list, String missioninfo, String missionname) {
        //create a text element
        final GUITextOverlay fullText = new GUITextOverlay(10,10, FontLibrary.FontSize.MEDIUM, getState());
        fullText.onInit();
        fullText.getText().add(missioninfo);
        fullText.autoHeight = true;

        final GUITextOverlay smallText = new GUITextOverlay(10,10, FontLibrary.FontSize.MEDIUM, getState());
        smallText.onInit();
        smallText.getText().add(missionname);
        smallText.autoHeight = true;

        GUITextButton b = new GUITextButton(getState(), (int) this.getWidth(), 30, fullText,this) {
            public GUITextOverlay full = fullText;
            public GUITextOverlay small = smallText;
            public GUITextOverlay active = smallText;
            public void displayText(boolean useFull) {
                if (useFull) {
                    active = full;
                } else {
                    active = small;
                }
                this.height=active.getHeight();
            }

            @Override
            public void onResize() {
                super.onResize();
                displayText(this.equals(selected));
            }
        };
        b.onInit();

        GUIListElement listElement = new GUIListElement(smallText,fullText,getState()) {

        };
        listElement.onInit(); //TODO resize list element?

        list.addWithoutUpdate(listElement);
        list.addWithoutUpdate(new GUIListElement(b,getState()));

    //   final GUITextButton button = new GUITextButton(getState(), bWidth, bHeight, "activate", new GUICallback() {
    //       @Override
    //       public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
    //           if (!mouseEvent.pressedLeftMouse())
    //               return;
    //           ModPlayground.broadcastMessage("clicky clicky");
    //       }

    //       @Override
    //       public boolean isOccluded() {
    //           return false;
    //       }
    //   });
    //   button.setColorPalette(GUITextButton.ColorPalette.NEUTRAL);
    //   button.onInit();
    //   list.addWithoutUpdate(new GUIListElement(button,getState()));
    }

    @Override
    public void draw() {
        super.draw();
    }

    @Override
    public void update(DrawerObservable drawerObservable, Object o, Object o1) {
    //    rebuildList();
    }

    @Override
    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
        if (mouseEvent.pressedLeftMouse()) {
            if (selected != null)
                selected.setSelected(false);
            if (guiElement.getUserPointer() instanceof GUIListElement) {
                guiElement = (GUIListElement)guiElement.getUserPointer();
            }
            if (guiElement instanceof GUIListElement) {
                ((GUIListElement) guiElement).setSelected(true);
            }
            ModPlayground.broadcastMessage("clicked" + guiElement.getClass().getSimpleName());
        } else {
            if (guiElement instanceof GUIListElement) {
                if (highlight != null) {
                    highlight.setHighlighted(false);
                }
                highlight = (GUIListElement) guiElement;
                if (highlight.getContent() instanceof GUIResizableElement) {
                    ((GUIResizableElement) highlight.getContent()).setWidth(this.getWidth());
                }
                highlight.setHighlighted(true);
            }
        }
    }

    @Override
    public boolean isOccluded() {
        return false;
    }

}

