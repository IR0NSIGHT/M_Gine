package me.iron.mGine.mod.missions;

import api.ModPlayground;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
import org.schema.schine.network.server.ServerMessage;

import java.util.HashSet;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.10.2021
 * TIME: 14:45
 * util class to help interacting with NPC diplomacy
 */
public class DiplomacyManager {
    public static DiplomacyManager instance;
    public DiplomacyManager() {
        instance = this;
    }

    public static boolean isReputationHighEnough(PlayerState playerState, int clientFactionID, ReputationRank requiredRank) {
        int diplPoints = getPlayerDiplPoints(playerState,clientFactionID);
        return requiredRank.getPoints()<=diplPoints;
    }

    public static ReputationRank getPlayerReputation(PlayerState playerState, int clientFactionID) {
        int points = getPlayerDiplPoints(playerState,clientFactionID);
        return ReputationRank.getReputation(points);
    }
    private static int getPlayerDiplPoints(PlayerState p, int factionID) {
        Faction f = GameServerState.instance.getFactionManager().getFaction(factionID);
        if (f instanceof NPCFaction) //TODO do all factions have a diplomacy ?
        {
            int diplPoints = 0;
            NPCDiplomacyEntity ent = ((NPCFaction) f).getDiplomacy().entities.get(p.getDbId());
            if (ent == null && p.getFactionId() != 0)
                ent = ((NPCFaction) f).getDiplomacy().entities.get(p.getFactionId());
            if (ent != null) {
                diplPoints = ent.getPoints();
            }
            return diplPoints;
        } else {
            return 0;
        }
    }

    public static void applyDiplomacyActions(HashSet<PlayerState> players, int clientFactionID, int pointsToAdd) {
        HashSet<Long> handledIDs = new HashSet<>(players.size());
        Faction f = GameServerState.instance.getFactionManager().getFaction(clientFactionID);
        if (!(f instanceof NPCFaction))
            return;

        for (PlayerState p: players) {
            long playerID = p.getFactionId();
            if (p.getFactionId() == 0)
                playerID = p.getDbId();
            if (handledIDs.contains(playerID))
                continue;
            handledIDs.add(playerID);
            NPCDiplomacyEntity ent = ((NPCFaction) f).getDiplomacy().entities.get(playerID);
            if (ent != null)
                ent.modPoints(pointsToAdd);
        }
        MissionUtil.notifyParty(players,"Your relations with " + f.getName()+ " have " + ((pointsToAdd>0)?"improved.":"worsened."), ServerMessage.MESSAGE_TYPE_INFO);
    }
}
