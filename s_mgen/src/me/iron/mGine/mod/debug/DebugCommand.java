package me.iron.mGine.mod.debug;

import api.ModPlayground;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.DataBaseManager;
import me.iron.mGine.mod.MissionUtil;
import me.iron.mGine.mod.network.MissionNetworkController;
import me.iron.mGine.mod.network.MissionPlayer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.server.ai.program.simpirates.PirateSimulationProgram;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.game.server.data.simulation.groups.AttackSingleEntitySimulationGroup;
import org.schema.game.server.data.simulation.groups.ShipSimulationGroup;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.game.server.data.simulation.jobs.SimulationJob;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.10.2021
 * TIME: 21:23
 */
public class DebugCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "mgine";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"mg"};
    }

    @Override
    public String getDescription() {
        return "mgine debug command:\n" +
                "mg synch (synch all players)\n"+
                "mg info (send info about mgine core)\n" +
                "mp ping (activate pingpong message)\n" +
                "mp restart (attempt restarting gameloop)\n" +
                "mp seeAll true (make this admin see all missions)";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        if (strings.length == 1 && strings[0].equals("synch")) {
            notify(playerState,"synching all players");
            MissionNetworkController.instance.setFlagUpdateSynchAll();
            return true;
        }
        if (strings.length == 1 && strings[0].equals("info")) {
            StringBuilder out = new StringBuilder("MGINE INFO\n");
            out.append("core has ").append(M_GineCore.instance.getMissions().size()).append(" missions.\n");
            out.append("last core update: ").append(MissionUtil.formatTime(M_GineCore.instance.lastUpdate)).append("\n");
            for (Mission m: M_GineCore.instance.getMissions()) {
                out.append(m.getName()).append("\n");
            }
            Collection<MissionPlayer> mps = MissionNetworkController.instance.getMissionPlayers();
            out.append("network controller has ").append(mps.size()).append(" players: \n");
            for (MissionPlayer mp: mps) {
                out.append(mp.toString()).append("\n");
            }
            notify(playerState,out.toString());
            return true;
        }
        if (strings.length == 2 && strings[0].equals("seeAll")) {
            boolean showAll = strings[1].equals("true");
            MissionPlayer p = MissionNetworkController.instance.getPlayerByName(playerState.getName());
            if (p == null) {
                notify(playerState,"no mp found for " + playerState.getName());
                return true;
            }
            p.setShowAll(showAll);
            p.flagUpdateAll();
            p.flagForSynch();
            return true;
        }
        if (strings.length == 1 && strings[0].equals("ping")) {
            M_GineCore.instance.pingPong = !M_GineCore.instance.pingPong;
            notify(playerState, "no mp found for " + playerState.getName());
            return true;
        }
        if (strings.length == 1 && strings[0].equals("restart")) {
            M_GineCore.instance.updateLoop(1);
            return true;
        }
        if (strings.length==1 && strings[0].equals("clear")) {
            M_GineCore.instance.clearMissions();
            ModPlayground.broadcastMessage("clearing all missions.");
            return true;
        }
        if (strings.length==2 && strings[0].equals("npc")) {
            try {
                int mod = Integer.parseInt(strings[1]);
                NPCFaction traders = DataBaseManager.instance.getNPCFactions().get(2);
                NPCDiplomacyEntity ent = traders.getDiplomacy().entities.get(playerState.getFactionId());
                for (int i = 0; i <3; i++) {
                    GameServerState.instance.getFactionManager().diplomacyAction(DiplomacyAction.DiplActionType.TRADING_WITH_US,playerState.getFactionId(),traders.getIdFaction());
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }


           return true;
        }
        return false;
    }

    private void spawnSimplePirates(PlayerState p) {
        Sendable selected = getSelectedObject(p);
        if (selected == null || !(selected instanceof ManagedUsableSegmentController)) {
            notify(p,"nothing/wrong selected");
            return;
        }
        ManagedUsableSegmentController msc = (ManagedUsableSegmentController) selected;
        SimulationManager simMan = GameServerState.instance.getSimulationManager();
        SimulationGroup simGroup = simMan.sendToAttackSpecific(msc,-1,3);
    }

    private void spawnAdvancedPirates(PlayerState p) {
        final Vector3i spawnPos = new Vector3i(p.getCurrentSector());
        final GameServerState state = GameServerState.instance;
        final String targetUID = p.getFirstControlledTransformableWOExc().getUniqueIdentifier();
        //create a job for the simulationManager to execute
        final SimulationJob simJob = new SimulationJob() {
            @Override
            public void execute(SimulationManager simMan) {
                Vector3i unloadedPos = simMan.getUnloadedSectorAround(spawnPos,new Vector3i());
                //create group
                ShipSimulationGroup myGroup = new AttackSingleEntitySimulationGroup(state,unloadedPos, targetUID);
                simMan.addGroup(myGroup);
                //spawn members
                int factionID = -1;
                CatalogPermission[] bps = simMan.getBlueprintList(3,1,factionID);
                if (bps.length == 0) {
                    new NullPointerException("no blueprints avaialbe for faction " + factionID).printStackTrace();
                    return;
                }
                myGroup.createFromBlueprints(unloadedPos,simMan.getUniqueGroupUId(),factionID,bps); //seems to try and work but doesnt spawn stuff?
                //add program to group
                PirateSimulationProgram myProgram = new PirateSimulationProgram(myGroup, false);
                myGroup.setCurrentProgram(myProgram);
            }
        };
        state.getSimulationManager().addJob(simJob); //adds job, is synchronized.
    }

    private FiniteStateMachine getCustomSimulationMachine(final AiEntityStateInterface obj, final PirateSimulationProgram program) {
        return null;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }

    private Sendable getSelectedObject(PlayerState p) {
        Sendable s = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(p.getSelectedEntityId());
        return s;
    }

    private void notify(PlayerState p, String mssg) {
        MissionUtil.notifyPlayer(p,mssg,ServerMessage.MESSAGE_TYPE_DIALOG);
    }

    class CustomSimulationMachine extends FiniteStateMachine {
        public CustomSimulationMachine(AiEntityStateInterface obj, MachineProgram program, Object parameter) {
            super(obj, program, parameter);
        }

        @Override
        public void createFSM(Object o) {

        }

        @Override
        public void onMsg(Message message) {

        }
    }
}

