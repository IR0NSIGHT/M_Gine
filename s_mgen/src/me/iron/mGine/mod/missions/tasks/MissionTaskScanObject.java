package me.iron.mGine.mod.missions.tasks;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.entity.EntityScanEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.MissionScout;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.HashSet;

public class MissionTaskScanObject extends MissionTask {

    private Listener<EntityScanEvent> listener;
    private boolean scanned;
    private SegmentController target;

    private String targetUID;
    public MissionTaskScanObject(Mission mission, String name, String info, boolean optional) {
        super(mission, name, info, optional);
        setIcon(MapIcon.WAYPOINT_MOVE);
        try {
            PlayerState p = GameServerState.instance.getPlayerStatesByName().values().iterator().next();
            Sector s = GameServerState.instance.getUniverse().getSector(p.getCurrentSector());
            for (SimpleTransformableSendableObject obj: s.getEntities()) {
                if (obj instanceof SpaceStation) {
                    targetUID = obj.getUniqueIdentifier();
                    setTarget((SegmentController)obj);
                    this.info = "Scan spacestation"+obj.getName() +" at " + obj.getSector(new Vector3i());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (target == null) {
            ModPlayground.broadcastMessage("NO TARGET FOR SCAN TASK");
        }
        init();
    }

    public void setTarget(SegmentController target) {
        this.target = target;
        this.setTaskSector(target.getSector(new Vector3i()));
        targetUID = target.getUniqueIdentifier();
    }

    /**
     * add to subscriber list for scan event, create listener if need be
     */
    private void init() {
        if (listener == null) {
            addScanListener();
        }
    }

    private void unregisterListener() {
        if (listener == null)
            return;
        StarLoader.getListeners(EntityScanEvent.class).remove(listener);
        listener = null; //kill reference, garbagecollector goes woosh
    }

    @Override
    public void update() {
        //update position
        if (target != null) {
            setTaskSector(target.getSector(new Vector3i()));
        }
    }

    public void onScan(EntityScanEvent event) {
        if (!event.isSuccess() || scanned)
            return;

        if (target == null && targetUID != null) {
            target = GameServerState.instance.getSegmentControllersByName().get(targetUID);
        }

        if (target == null)
            return;

        for (PlayerState p: mission.getActiveParty()) {
        if (p.equals(event.getOwner()) && !target.isCloakedFor(event.getEntity())) {

                //this is the droid you are looking for
                scanned = true;
                setCurrentState(MissionState.SUCCESS);
                new StarRunnable(){
                    @Override
                    public void run() {
                        unregisterListener(); //TODO use starloader method once PR is through
                    }
                }.runLater(ModMain.instance,10);
            }
        }
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
