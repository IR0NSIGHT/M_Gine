package me.iron.mGine.mod.clientside.map;

import api.listener.Listener;
import api.listener.events.input.MousePressEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.GameMapDrawListener;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import org.lwjgl.util.vector.Vector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 21.09.2021
 * TIME: 13:55
 * clientside fastutil listener that draws to the map
 */
public class MissionMapDrawer implements GameMapDrawListener {
    public static MissionMapDrawer instance;
    private static final float sectorScale = 100f/ VoidSystem.SYSTEM_SIZE;
    private static final Vector3f halfSectorOffset = new Vector3f(sectorScale/2f,sectorScale/2f,sectorScale/2f);

    //internal stuff for drawing
    private HashMap<Sprite,MapMarker[]> sprite_to_subsprites = new HashMap<>(); //internal mapping from sprite->subsprite for drawer
    private HashSet<MapMarker> mapMarkers = new HashSet<>(); //list of all markers to draw, provided by server

    //non drawing stuff for verwaltung
    private HashMap<UUID,MapMarker> openMarkers = new HashMap<>(); //list of all "open mission !" map markers

    private Vector3f centerOn;
    public MapMarker selected;
    public MissionMapDrawer() {
        super();
        instance = this;
        FastListenerCommon.gameMapListeners.add(this);
        StarLoader.registerListener(MousePressEvent.class, new Listener<MousePressEvent>() {
            @Override
            public void onEvent(MousePressEvent mouseEvent) {
                if (mouseEvent.getRawEvent().pressedLeftMouse() && selected != null) {
                    if (selected instanceof MissionMarker && !((MissionMarker)selected).isHidded()) {
                        MissionMarker m = (MissionMarker)selected;
                        Mission mm = MissionClient.instance.getMission(m.getUuid());
                        if (mm != null)
                            MissionClient.instance.setSelectedMission(mm);
                    } else {
                        centerOn = new Vector3f(selected.getSector().x,selected.getSector().y,selected.getSector().z);
                    }
                }
                if (mouseEvent.getRawEvent().pressedRightMouse() && selected != null) {
                    MissionClient.instance.navigateTo(selected.getSector());
                }
            }
        }, ModMain.instance);
    }

    /**
     * will add the marker, requires updateLists() to become effective
     * @param marker
     * @return true if added, false if already exists, no update required.
     */
    public void addMarker(MapMarker marker) {
        mapMarkers.add(marker);
    }

    /**
     * will remove a marker from the lists. requires updateInternalList to be applied
     * @param marker
     */
    public void removeMarker(MapMarker marker) {
        mapMarkers.remove(marker);
    }

    public HashSet<MapMarker> getMapMarkers() {
        return mapMarkers;
    }

    /**
     * will copy internal mapping of sprite->subsprite hashset to sprite->subsprite[]
     */
    public void updateInternalList() {

        HashMap<Sprite, HashSet<MapMarker>> sprite_to_subsprites_set = new HashMap<>();
        sprite_to_subsprites.clear();
        //collect all markers, sorted by their sprite:
        for (MapMarker marker: mapMarkers) {
            Sprite sprite = marker.getSprite();
            if (sprite == null)
                continue;

            //get set
            HashSet<MapMarker> subsprites = sprite_to_subsprites_set.get(sprite);
            if (subsprites == null) {
                subsprites = new HashSet<MapMarker>();
                sprite_to_subsprites_set.put(sprite,subsprites);
            }
            subsprites.add(marker);
        }

        //build the sprite vs SubSprite[] list for drawing
        for (Map.Entry<Sprite,HashSet<MapMarker>> entry: sprite_to_subsprites_set.entrySet()) {
            MapMarker[] arr = entry.getValue().toArray(new MapMarker[0]);
            //TODO remove
            if (entry.getKey() == null)
                continue;

            sprite_to_subsprites.put(entry.getKey(),arr);
        }
    }

    public void updateOpenMarkers(HashMap<UUID,Vector3i> newMarkers) {
        //delete old ones that are obsolete
        for (Map.Entry<UUID,MapMarker> entry: openMarkers.entrySet()) {
            if (!newMarkers.containsKey(entry.getKey())) {
                removeMarker(entry.getValue());
            }
        }

        //add new ones that dont exist yet
        for (Map.Entry<UUID,Vector3i> entry: newMarkers.entrySet()) {
            if (!openMarkers.containsKey(entry.getKey())) {
                MissionMarker mm = new MissionMarker(entry.getValue(),"Open mission",entry.getKey(), MapIcon.WP_QUEST, MapIcon.WP_QUEST_CIRCLE, brightYellow);
                Vector3f halfSector = new Vector3f(posFromSector(new Vector3i(1,1,1),false));
                halfSector.scale(0.25f);
                mm.getPos().add(halfSector);
                addMarker(mm);
                openMarkers.put(entry.getKey(),mm);
            } else {
                MapMarker m = openMarkers.get(entry.getKey());
                m.setSector(entry.getValue());
            }
        }
        MissionMapDrawer.instance.updateInternalList();
    }

    @Override
    public void system_PreDraw(GameMapDrawer gameMapDrawer, Vector3i vector3i, boolean b) {
        if (centerOn != null) {
            gameMapDrawer.getGameMapPosition().set((int)centerOn.x,(int)centerOn.y,(int)centerOn.z,true);
            centerOn = null;
        }
    }

    @Override
    public void system_PostDraw(GameMapDrawer gameMapDrawer, Vector3i vector3i, boolean b) {
    }

    @Override
    public void galaxy_PreDraw(GameMapDrawer gameMapDrawer) {
        for (MapMarker m: mapMarkers) {
            if (m instanceof TaskMarker) {
                ((TaskMarker) m).updateFromTask();
            }
        }
    }

    @Override
    public void galaxy_PostDraw(GameMapDrawer gameMapDrawer) {

    }

    @Override
    public void galaxy_DrawLines(GameMapDrawer gameMapDrawer) {
        try {
            for (MapMarker m: mapMarkers) {
                if (m instanceof TaskMarker) {
                    ((TaskMarker)m).drawLines(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void galaxy_DrawSprites(GameMapDrawer gameMapDrawer) {
        for (Map.Entry<Sprite,MapMarker[]> entry: sprite_to_subsprites.entrySet()) {
            for (MapMarker m: entry.getValue()) {
                m.preDraw(gameMapDrawer);
            }
            DrawUtils.drawSprite(gameMapDrawer,entry.getKey(),entry.getValue());
        }
    }

    @Override
    public void galaxy_DrawQuads(GameMapDrawer gameMapDrawer) {

    }

    public void drawLinesSector(Vector3i from, Vector3i to, Vector4f startColor, Vector4f endColor) {
        Vector3f start = posFromSector(from,false);
        Vector3f end = posFromSector(to,false);
        DrawUtils.drawFTLLine(start,end,startColor,endColor);
    }

    public void drawText(Vector3i sector, String text) {
        drawText(posFromSector(sector,true),text);
    }

    public void drawText(Vector3f mapPos, String text) {
        Transform t = new Transform();
        t.setIdentity();
        t.origin.set(mapPos);
        ConstantIndication indication = new ConstantIndication(t, Lng.str(text));
        HudIndicatorOverlay.toDrawMapTexts.add(indication);
    }

    //helper stuff //TODO move to UTIL
    public static Vector3f posFromSector(Vector3i sector, boolean isSprite) {

        Vector3f out = sector.toVector3f();
        if (isSprite) {
            out.add(new Vector3f(-VoidSystem.SYSTEM_SIZE_HALF,-VoidSystem.SYSTEM_SIZE_HALF,-VoidSystem.SYSTEM_SIZE_HALF));
        }
        out.scale(sectorScale); out.add(halfSectorOffset);
        return out;
    }

    public static Vector4f colorByMissionState(MissionState state) {
        switch (state) {
            case SUCCESS:
                return brightGreen;
            case FAILED:
                return brightRed;
            case IN_PROGRESS:
                return brightYellow;
            case OPEN:
                return brightOrange;
            case ABORTED:
                return darkRed;
            default:
                return grey;
        }
    }

    public static Vector4f brightOrange = new Vector4f(1f,0.5f,0,1);
    public static Vector4f brightYellow = new Vector4f(0.97f,1.0f,0,1);
    public static Vector4f darkRed = new Vector4f(0.5f,0f,0,1);
    public static Vector4f brightRed = new Vector4f(1f,0f,0,1);
    public static Vector4f brightGreen = new Vector4f(0,1,0,1);
    public static Vector4f darkGreen = new Vector4f(0,0.5f,0,1);
    public static Vector4f grey = new Vector4f(0.5f,0.5f,0.5f,1);
    public static float scale32px = 0.2f;
}
