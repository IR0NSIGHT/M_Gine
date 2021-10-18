package me.iron.mGine.mod.missions.tasks;

import api.DebugFile;
import api.ModPlayground;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.missions.MissionUtil;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.util.Set;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.10.2021
 * TIME: 17:27
 * go to this sector, near this entity, stand still and stay until the cargo is unloaded/loaded.
 * can do multiple tours, success once all units are delivered/received.
 * target stationwrapper MUST be complete: UID, pos etc.
 */
public class MissionTaskUnloadCargo extends MissionTask {
    private short blockID;
    private int units;
    private int unitsStart;
    private float blockVolume;
    private String blockName;
    private DataBaseStation target;
    private int infoIntervall = 0;
    private boolean load;

    public MissionTaskUnloadCargo(Mission mission, String name, String info, DataBaseStation target, short blockType, int units, boolean load,boolean optional) {
        super(mission, name, info, optional);
        this.target = target;
        setCargo(blockType,units);
        setIcon(MapIcon.WP_DROPOFF);
        setTaskSector(target.getPosition());
        setLoad(load);
    }

    public void setCargo(short blockID, int units) {
        ElementInformation ei =  ElementKeyMap.getInformationKeyMap().get(blockID);
        if (ei == null)
            return;

        this.blockID = blockID;
        this.units = units;
        this.unitsStart = units;
        this.blockVolume = ei.getVolume();
        this.blockName = ei.getName();
    }

    /**
     * @param load true: load into ship, false: load out of ship
     */
    public void setLoad(boolean load) {
        this.load = load;
        setIcon(load?MapIcon.WP_PICKUP:MapIcon.WP_DROPOFF);
    }

    @Override
    public void update() {
        super.update();
        if (!currentState.equals(MissionState.IN_PROGRESS))
            return;

        if (units == 0)
            return;
        infoIntervall ++;
        for (PlayerState p: mission.getActiveParty()) {
            tryUnload(p);
        }
    }

    @Override
    protected boolean successCondition() {
        return (units == 0) || currentState.equals(MissionState.SUCCESS);
    }

    private void tryUnload(PlayerState player) {

        if (!player.getCurrentSector().equals(target.getPosition()))
            return;

        //special case for random stations that might not exist yet. attempt to get any station in the target sector with a matching faction ID.
        if (target.getUID().equals("")) {
            try {
                Set<SimpleTransformableSendableObject<?>> entitiesInTargetSector = GameServerState.instance.getUniverse().getSector(target.getPosition()).getEntities();
                for (SimpleTransformableSendableObject<?> obj: entitiesInTargetSector) {
                    if (obj instanceof SpaceStation && obj.getFactionId()==target.getFactionID()) {
                        target.setUID(obj.getUniqueIdentifier());
                    }
                }
            } catch (Exception ex) {
                DebugFile.logError(new NullPointerException("target station for transport mission doesn't exist in sector " + target.getPosition() +" :" + target.getUID()), ModMain.instance);
                MissionUtil.notifyParty(mission.getActiveParty(),"target station doesn't exist. contact admin.",ServerMessage.MESSAGE_TYPE_ERROR);
                return;
            }
        }

        //get the segmentcontorller by UID
        Sendable s = GameServerState.instance.getLocalAndRemoteObjectContainer().getUidObjectMap().get(target.getUID());
        if (!(s instanceof SegmentController)) {
            DebugFile.logError(new NullPointerException("target station for transport mission doesn't exist in sector " + target.getPosition() +" :" + target.getUID()), ModMain.instance);
            MissionUtil.notifyParty(mission.getActiveParty(),"target station doesn't exist. contact admin.",ServerMessage.MESSAGE_TYPE_ERROR);
            return;
        }

        SegmentController sc = (SegmentController)s;        //GameServerState.instance.getSegmentControllersByName().get(target.getUID());

        //get distance to station based on bounding sphere of ship and station
        Vector3f playerVsStationOffset = sc.getWorldTransform().origin;
        SimpleTransformableSendableObject playerS = player.getFirstControlledTransformableWOExc();
        if (!(playerS instanceof SegmentController)) {
            inform(player,"Must be in a ship");
            return;
        }

        if (playerS instanceof Ship && ((Ship)playerS).railController.isDocked()) {
            playerS = ((Ship) player.getFirstControlledTransformableWOExc()).railController.getRoot();
        }

        playerVsStationOffset.sub(playerS.getWorldTransform().origin);
        float minDist =Math.round(2*(sc.getBoundingSphereTotal().radius+playerS.getBoundingSphereTotal().radius));

        if (playerVsStationOffset.length()>= minDist) {
            inform(player,"Must be closer than "+minDist+"m to station " + target.getName() +".");
            return;
        }

        Inventory inv = player.getInventory();

        if (!load && !inv.existsInInventory(blockID)) {
            inform(player,"No " + blockName +" detected in your inventory.");
            return;
        }

        if (!player.isUseCargoInventory()) {
            inform(player,"Must select cargo in player inventory.");
            return;
        }


        try {
            int amount;
            if (load) {
                int wantToAdd = (int)Math.min(1000/blockVolume,units);
                amount = inv.canPutInHowMuch(blockID,wantToAdd,-1);
                if (amount == 0) {
                    inform(player,"cargo is full.");
                    return;
                }
                inform(player,"loading cargo: " + Math.round((1-(float)units/(float)unitsStart)*100)+"%"); //TODO custom hud thingy
            } else { //unload
                amount = (int)-Math.min((1000/blockVolume),units);
                inform(player,"unloading cargo: " +Math.round((1-(float)units/(float)unitsStart)*100)+"%");
            }
            int slot = inv.incExistingOrNextFreeSlot(blockID,amount);
            units -= Math.abs(amount);
            inv.sendInventoryModification(slot);
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
        return (load?"Load ":"Unload ") + MissionUtil.formatMoney(units) + "x "+ blockName+" at " + target.getName()+" " + target.getPosition().toStringPure();
    }

    public short getBlockID() {
        return blockID;
    }

    public int getUnits() {
        return units;
    }

    public int getUnitsStart() {
        return unitsStart;
    }

    public float getBlockVolume() {
        return blockVolume;
    }

    public String getBlockName() {
        return blockName;
    }
}
