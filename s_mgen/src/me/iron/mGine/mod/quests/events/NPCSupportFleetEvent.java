package me.iron.mGine.mod.quests.events;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.mGine.mod.ModMain;
import org.lwjgl.util.vector.Vector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.network.objects.remote.FleetCommand;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFleetManager;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemFleetManager;

import java.util.List;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 24.10.2021
 * TIME: 14:12
 * NPC factions sends a fleet to support player
 */
public class NPCSupportFleetEvent {
    private NPCFaction faction;
    private NPCFleetManager fleetManager;
    private Fleet fleet;

    private boolean startSectorReached;
    private boolean endSectorReached;
    private Vector3i startSector;
    private Vector3i targetSector;
    private int waitAtStartTime = 30;
    private int waitAtTargetTime = 60;
    private boolean flagForRemove;
    private boolean deleted;
    public NPCSupportFleetEvent(NPCFaction faction, Vector3i start, Vector3i target, long seed) {
        Random r = new Random(seed);
        this.faction = faction;
        this.fleetManager = faction.getFleetManager();
        this.startSector = start;
        this.targetSector = target;

        updateLoop();
        fleet = faction.getFleetManager().spawnFleet(target,3+r.nextInt(8));

        for (FleetMember m: fleet.getMembers()) {
            m.getSector().set(start);
        }
        ModPlayground.broadcastMessage("spawned attack fleet for faction " + faction.getName() + " target " + target);
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (event.getText().contains("STOP")) {
                    flagForRemove = true;
                    final Listener<PlayerChatEvent> l = this;
                    new StarRunnable(){
                        @Override
                        public void run() {
                            StarLoader.unregisterListener(PlayerChatEvent.class,l);
                        }
                    }.runLater(ModMain.instance,200);
                }
            }
        },ModMain.instance);
    }

    private void updateLoop() {
        new StarRunnable(){
            long last = System.currentTimeMillis();
            @Override
            public void run() {
                if (last + 1000 < System.currentTimeMillis()) {
                    last = System.currentTimeMillis();
                    try {
                        if(deleted) {
                            ModPlayground.broadcastMessage("fleet not in fleetcache.");
                            cancel();
                        }
                        update();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.runTimer(ModMain.instance,10);
    }
    private void update() {
        List<Fleet> fleets = fleetManager.fleets;

        if (flagForRemove) { //delete unloaded members
            for (FleetMember member: fleet.getMembers()) {
                if (GameServerState.instance.getUniverse().isSectorLoaded(member.getSector())) {
                    ModPlayground.broadcastMessage("removing unloaded member " + member.name +" in " + member.getSector().toStringPure());
                    fleet.removeMemberByDbIdUID(member.entityDbId,true);
                }
            }
            if (fleet.getMembers().isEmpty()) {
                fleetManager.getFleetManager().removeFleet(fleet);
                deleted = true;
            }
        }

        NPCSystemFleetManager.FleetType type = fleet.getNpcType();
        Vector3i currentSector = fleet.getFlagShip().getSector();
        if (fleet.getMembers().size()>1) {
            currentSector = fleet.getMembers().get(1).getSector(); //flag ship likes to chill at -245^3, idk why
        }
        for (FleetMember fm : fleet.getMembers()) {
            if (fm.getSector().equals(startSector)) {
                startSectorReached = true;
                break;
            }
        }
        if (!startSectorReached && currentSector.equals(startSector))
            startSectorReached = true;

        //make fleet go to start
        if (!startSectorReached && !fleet.getCurrentMoveTarget().equals(startSector)) {
            fleetMoveTo(fleet,startSector);
            return;
        }

        //make fleet go to target after reaching start;
        if (startSectorReached && !fleet.getCurrentMoveTarget().equals(targetSector)) {
            if (waitAtStartTime > 0) {
                waitAtStartTime--;
                ModPlayground.broadcastMessage("waiting time left " + waitAtStartTime);
                return;
            }
            fleetAttackSector(fleet,targetSector);
            return;
        }

        if (currentSector.equals(targetSector))
            endSectorReached = true;

        if (endSectorReached)
            waitAtTargetTime--;

        if (waitAtStartTime <= 0)
            flagForRemove = true;



        Vector3i off = new Vector3i(fleet.getCurrentMoveTarget());
        off.sub(currentSector);
        ModPlayground.broadcastMessage("fleet on way, target: " + fleet.getCurrentMoveTarget() + " dist to target: " + off.length());
        StringBuilder out = new StringBuilder();
        for (FleetMember member: fleet.getMembers()) {
            out.append(member.getSector().toStringPure()).append(": ").append(member.name).append("\n");
        }
        ModPlayground.broadcastMessage(out.toString());
    }

    private void fleetMoveTo(Fleet fleet, Vector3i sector) {
        FleetCommand com = new FleetCommand(FleetCommandTypes.MOVE_FLEET, fleet, sector);
        fleetManager.getFleetManager().executeCommand(com);

    //    fleet.sendFleetCommand(FleetCommandTypes.MOVE_FLEET,sector);
        ModPlayground.broadcastMessage("fleet move to" + sector.toStringPure());
    }

    private void fleetAttackSector(Fleet f, Vector3i sector) {
        FleetCommand com = new FleetCommand(FleetCommandTypes.FLEET_ATTACK, fleet, sector);
        fleetManager.getFleetManager().executeCommand(com);
    //    f.sendFleetCommand(FleetCommandTypes.FLEET_ATTACK,sector);
        ModPlayground.broadcastMessage("fleet attack sector" + sector.toStringPure());
    }
}
























