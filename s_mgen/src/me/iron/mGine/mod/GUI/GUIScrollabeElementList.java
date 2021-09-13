package me.iron.mGine.mod.GUI;

import api.ModPlayground;
import me.iron.mGine.mod.generator.Mission;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;

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

    private ArrayList<Mission> missions = new ArrayList<>();

    public GUIScrollabeElementList(float width, float height, GUIElement dependent, InputState inputState) {
        super(width, height, dependent, inputState);
    }

    public void setMissions(Collection<Mission> missions) {
        for (Mission m: this.missions) {
            m.deleteObserver(this);
        }
        this.missions = (ArrayList<Mission>) missions;
        for (Mission m: missions) {
            m.addObserver(this);
        }
        updateList();
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
            for (int i = 0; i < list.size(); i++) {
                //overwrite the text of each mission
                GUIListElement listEl = list.get(i);
                if (listEl.getContent() instanceof GUITextOverlay) {
                    GUITextOverlay content = (GUITextOverlay) listEl.getContent();
                    content.getText().clear();
                    String text = (i< missions.size())?missions.get(i).getDescription():"";
                    content.getText().add(text);
                }
            }

        //   list.clear();
        //   list.deleteObservers();
        //   for (Mission m: missions) {
        //       m.clearObservers();
        //       m.addObserver(this);
        //       //todo make small text contain name,reward,time
        //       addTextRow(list,m.getDescription(),m.getType().name());
        //   }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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
        updateList();
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

