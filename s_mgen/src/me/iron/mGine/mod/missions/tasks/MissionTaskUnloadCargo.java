package me.iron.mGine.mod.missions.tasks;

import api.DebugFile;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.MissionUtil;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import org.hsqldb.server.Server;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.10.2021
 * TIME: 17:27
 * go to this sector, near this entity, stand still and stay until the cargo is unloaded.
 * can do multiple tours, success once all units are delivered.
 * target stationwrapper MUST be complete: UID, pos etc.
 */
public class MissionTaskUnloadCargo extends MissionTask {
    private short blockID;
    private int units;
    private float blockVolume;
    private String blockName;
    private DataBaseStation target;
    private int infoIntervall = 0;

    public MissionTaskUnloadCargo(Mission mission, String name, String info, DataBaseStation target, short blockType, int units, boolean optional) {
        super(mission, name, info, optional);
        this.target = target;
        setCargo(blockType,units);
        setIcon(MapIcon.WP_DROPOFF);
        setTaskSector(target.getPosition());
    }

    public void setCargo(short blockID, int units) {
        ElementInformation ei =  ElementKeyMap.getInformationKeyMap().get(blockID);
        if (ei == null)
            return;

        this.blockID = blockID;
        this.units = units;
        this.blockVolume = ei.getVolume();
        this.blockName = ei.getName();
    }

    @Override
    public void update() {
        super.update();
        if (units == 0)
            return;
        infoIntervall ++;
        for (PlayerState p: mission.getActiveParty()) {
            tryUnload(p);
        }
    }

    @Override
    protected boolean successCondition() {
        return super.successCondition() || units == 0;
    }

    private void tryUnload(PlayerState player) {

        if (!player.getCurrentSector().equals(target.getPosition()))
            return;

        Sendable s = GameServerState.instance.getLocalAndRemoteObjectContainer().getUidObjectMap().get(target.getUID());
        if (s == null || !(s instanceof SegmentController))
            return;

        SegmentController sc = (SegmentController)s;        //GameServerState.instance.getSegmentControllersByName().get(target.getUID());

        if (sc == null) {
            //station is not here.
            DebugFile.logError(new NullPointerException("target station for transport mission doesnt exist in sector " + target.getPosition() +" :" + target.getUID()), ModMain.instance);
            MissionUtil.notifyParty(mission.getActiveParty(),"target station doesn't exist. contact admin.",ServerMessage.MESSAGE_TYPE_ERROR);
        //    mission.setState(MissionState.FAILED);
            return;
        }

        Vector3f playerVsStationOffset = sc.getWorldTransform().origin;
        playerVsStationOffset.sub(player.getFirstControlledTransformableWOExc().getWorldTransform().origin);
        float minDist =Math.round(2*(sc.getBoundingSphereTotal().radius+player.getFirstControlledTransformableWOExc().getBoundingSphereTotal().radius));

        if (playerVsStationOffset.length()>= minDist) {
            inform(player,"Must be closer than "+minDist+"m to station " + target.getName() +".");
            return;
        }

        Inventory inv = player.getInventory();

        if (!inv.existsInInventory(blockID)) {
            inform(player,"No " + blockName +" detected in your inventory.");
            return;
        }

        try {
            int needed = Math.min((int)(1000/blockVolume),units);
            int slot = inv.incExistingOrNextFreeSlot(blockID, -needed);
            inv.sendInventoryModification(slot);
            units -= needed;
            inform(player,"unloading cargo.");
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void inform(PlayerState p,String mssg) {
        if (infoIntervall < 4)
            return;
        infoIntervall = 0;
        p.sendServerMessage(Lng.astr(mssg), ServerMessage.MESSAGE_TYPE_INFO);
    }

    @Override
    public String getTaskSummary() {
        return "Unload " + units + "x "+ blockName+" at " + target.getName()+" " + target.getPosition().toStringPure();
    }
}
