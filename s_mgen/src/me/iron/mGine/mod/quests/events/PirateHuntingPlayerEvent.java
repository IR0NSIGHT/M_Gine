package me.iron.mGine.mod.quests.events;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.simpirates.PirateSimulationProgram;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.game.server.data.simulation.groups.AttackSingleEntitySimulationGroup;
import org.schema.game.server.data.simulation.groups.ShipSimulationGroup;
import org.schema.game.server.data.simulation.jobs.SimulationJob;

import java.util.Collection;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 24.10.2021
 * TIME: 13:39
 * a group of AI ships will start hunting the player
 */
public class PirateHuntingPlayerEvent {
    public PirateHuntingPlayerEvent(PlayerState p, int factionID, long seed) {
        Random r = new Random(seed);
        final GameServerState state = GameServerState.instance;
        SimpleTransformableSendableObject playerObj = p.getFirstControlledTransformableWOExc();
        if (playerObj == null)
            return;

        state.getSimulationManager().sendToAttackSpecific(playerObj,factionID,1+r.nextInt(4));
    }
}

