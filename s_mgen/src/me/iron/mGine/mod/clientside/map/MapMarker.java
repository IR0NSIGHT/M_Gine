package me.iron.mGine.mod.clientside.map;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.entry.SelectableMapEntry;
import org.schema.game.client.view.camera.GameMapCamera;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Objects;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.09.2021
 * TIME: 11:31
 * container class that is basically:
 * name, position and icon to be drawn on map
 */
public class MapMarker implements PositionableSubColorSprite, SelectableSprite, SelectableMapEntry {
    /**
     * make a new mapmarker.
     * @param icon icon to show on map, enum
     */
    public MapMarker(Vector3i sector, String name, MapIcon icon, Vector4f color) {
    //    ModPlayground.broadcastMessage("new marker " + name + icon.name());
        this.color.set(color);
        this.sector.set(sector);
        this.name = name;
        this.icon = icon;
        this.mapPos = MissionMapDrawer.posFromSector(sector,true);
    }

    MapIcon icon;
    Vector3i sector = new Vector3i();
    String name;
    Vector4f color = new Vector4f();
    Vector3f mapPos;

    transient private boolean selected;
    transient private boolean drawIndication;
    private float baseScale = 0.1f; //base scale
    public float scaleFactor = 1; //zoom factor: grow when further away

    /**
     * code that gets called before the marker is drawn.
     */
    public void preDraw(GameMapDrawer drawer) {
        autoScale(drawer.getCamera());
        if (selected) {
            Vector3f pos = new Vector3f(getPos());
            pos.sub(drawer.getCamera().getPos());
            float distToCam = pos.length()*0.005f;
            Vector3f textPos = new Vector3f(drawer.getCamera().getCachedUp());
            Vector3f right = new Vector3f(drawer.getCamera().getCachedRight());
            right.scale(distToCam* getMapText().length()/2f);
            textPos.scale(Math.max(distToCam*7,12f));
            textPos.add(getPos());
            textPos.add(right);
            MissionMapDrawer.instance.drawText(textPos,getMapText());
        }
    }

    public String getMapText() {
        return name;
    }

    public void addToDrawList(boolean isPublic) {
        MissionMapDrawer.instance.addMarker(this);
    }

    public void removeFromDrawList() {
        //TODO remove method in drawer
        //EdenMapDrawer.instance.
    }

    protected void autoScale(GameMapCamera camera) {
        Vector3f distanceToCam = new Vector3f(camera.getPos());
        distanceToCam.sub(mapPos);
        float dist = distanceToCam.length();
        scaleFactor = Math.min(10,Math.max(1,dist/300));
    }

    public Sprite getSprite() {
        if (icon == null)
            return null;
        return icon.getSprite();
    }

    @Override
    public Vector4f getColor() {
        return color;
    }

    @Override
    public float getScale(long l) {
    //    ModPlayground.broadcastMessage("scale "+ scale+"factor " + scaleFactor + " selected" + selected);
        return baseScale * scaleFactor * (selected?2:1);
    }

    @Override
    public int getSubSprite(Sprite sprite) {
        return icon.getSubSprite();
    }

    @Override
    public boolean canDraw() {
        return true;
    }

    @Override
    public Vector3f getPos() {
        return mapPos;
    }

    @Override
    public boolean isDrawIndication() {
        return drawIndication;
    } //??

    @Override
    public void setDrawIndication(boolean b) {

    }//??

    public MapIcon getIcon() {
        return icon;
    }

    public void setIcon(MapIcon icon) {
        this.icon = icon;
    }

    public Vector3i getSector() {
        return sector;
    }

    public void setSector(Vector3i sector) {
        this.sector.set(sector);
        this.mapPos = MissionMapDrawer.posFromSector(sector,true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(Vector4f color) {
        this.color.set(color);
    }

    public void setMapPos(Vector3f mapPos) {
        this.mapPos = mapPos;
    }

    public float getBaseScale() {
        return baseScale;
    }

    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public float getSelectionDepth() {
        return 0;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void onSelect(float v) {
        selected = true;
        MissionMapDrawer.instance.selected = this;
    }

    @Override
    public void onUnSelect() {
        if (!selected)
            return;

        selected = false;
    //    ModPlayground.broadcastMessage("unselect" + this.name);
        if (MissionMapDrawer.instance.selected != null && MissionMapDrawer.instance.selected.equals(this))
            MissionMapDrawer.instance.selected = null;

    }

    @Override
    public String toString() {
        return "MapMarker{" +
                "icon=" + icon +
                ", sector=" + sector +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", pos=" + mapPos +
                ", scale=" + baseScale +
                ", scaleFactor=" + scaleFactor +
                ", drawIndication=" + drawIndication +
                '}';
    }

    public boolean getSelected() {
        return selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapMarker marker = (MapMarker) o;
        return icon == marker.icon &&
                sector.equals(marker.sector) &&
                name.equals(marker.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon, sector, name);
    }
}
