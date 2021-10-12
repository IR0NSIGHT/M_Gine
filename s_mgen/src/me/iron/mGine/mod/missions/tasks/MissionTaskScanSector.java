package me.iron.mGine.mod.missions.tasks;

import api.listener.Listener;
import api.listener.events.entity.EntityScanEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;

public class MissionTaskScanSector extends MissionTask {

    private transient Listener<EntityScanEvent> listener;
    private boolean scanned;

    public MissionTaskScanSector(Mission mission, String name, String info, Vector3i sector, boolean optional) {
        super(mission, name, info, optional);
        setTaskSector(sector);
        setIcon(MapIcon.WP_SCAN);
        init();
    }

    /**
     * add to subscriber list for scan event, create listener if need be
     */
    private void init() {
        if (listener == null) {
            addScanListener();
        }
    }

    protected void unregisterListener() {
        if (listener == null)
            return;
        StarLoader.unregisterListener(EntityScanEvent.class,listener);
        listener = null; //kill reference, garbagecollector goes woosh
    }

    @Override
    protected boolean successCondition() {
        return scanned;
    }

    public void onScan(EntityScanEvent event) {
        if (!event.isSuccess() || scanned)
            return;

        if (!event.getEntity().getSector(new Vector3i()).equals(getTaskSector()))
            return;

        if (!scannerRunByParty(event))
            return;


        //this is the droid you are looking for
        scanned = true;
        new StarRunnable(){
            @Override
            public void run() {
                unregisterListener(); //TODO use starloader method once PR is through
            }
        }.runLater(ModMain.instance,10);
    }

    protected boolean scannerRunByParty(EntityScanEvent event) {
        for (PlayerState p: mission.getActiveParty()) {
            if (p == null)
                continue;

            if (p.equals(event.getOwner())) {
                return true;
            }
        }
        return false;
    }

    /**
     * one listener for all scoutmissions.
     */
    private void addScanListener() {
        listener = new Listener<EntityScanEvent>() {
            @Override
            public void onEvent(EntityScanEvent entityScanEvent) {
                onScan(entityScanEvent);
            }
        };
        StarLoader.registerListener(EntityScanEvent.class, listener, ModMain.instance);
    }
}
