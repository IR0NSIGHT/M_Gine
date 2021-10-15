package me.iron.mGine.mod.clientside.map;

import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.generator.Mission;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.camera.GameMapCamera;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.UUID;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.10.2021
 * TIME: 16:02
 */
public class MissionMarker extends MapMarker {
    private Mission mission;
    private MapIcon hiddenIcon;
    private UUID uuid;
    /**
     * make a new mapmarker.
     *
     * @param sector
     * @param color
     */
    public MissionMarker(Vector3i sector, String name, UUID uuid, MapIcon defaultIcon, MapIcon hiddenIcon, Vector4f color) {
        super(sector, name, defaultIcon, color);
        this.setBaseScale(getBaseScale()/3);
        this.hiddenIcon = hiddenIcon;
        this.uuid = uuid;
    }

    @Override
    public void preDraw(GameMapDrawer drawer) {
        updateIsHidden();
        autoScale(drawer.getCamera());
        super.preDraw(drawer);
    }

    @Override
    protected void autoScale(GameMapCamera camera) {
        Vector3f distanceToCam = new Vector3f(camera.getPos());
        distanceToCam.sub(mapPos);
        float dist = distanceToCam.length();
        scaleFactor = Math.min(30, Math.max(1.5f,dist/100));

    }

    @Override
    public int getSubSprite(Sprite sprite) {
        if (mission != null)
            return hiddenIcon.getSubSprite();
        return super.getSubSprite(sprite);
    }

    @Override
    public boolean canDraw() {
       return MissionClient.instance.isDrawOpenMarkers() && (MissionClient.instance.getSelectedMission()==null || !MissionClient.instance.getSelectedMission().getUuid().equals(uuid));
    }

    @Override
    public String getMapText() {
       if (mission != null)
           return mission.getName();
       return super.getMapText();
    }

    private void updateIsHidden() {
        mission = MissionClient.instance.getMission(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isHidded() {
        return mission == null;
    }
}
