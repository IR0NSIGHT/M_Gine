package me.iron.mGine.mod;

import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 24.10.2021
 * TIME: 13:42
 */
public class CatalogUtil {
    public static ArrayList<CatalogPermission> getBlueprints(int factionID, int minMass, int maxMass,BlueprintType type, @Nullable BlueprintClassification ... classifications) {
        ArrayList<CatalogPermission> candidates = new ArrayList<>();
        for (CatalogPermission bp: getCatalog()) {
            //TODO make check for faction ID allowance
            if (!bp.enemyUsable())
                continue;

            if (classifications != null) {
                boolean allow = false;
                for (BlueprintClassification c: classifications) {
                    if (c.equals(bp.getClassification())) {
                        allow = true;
                        break;
                    }
                }
                if (!allow)
                    continue;
            }

            if (!bp.type.equals(type))
                continue;

            if (bp.mass<minMass && bp.mass>maxMass)
                continue;

            candidates.add(bp);
        }
        return candidates;
    }

    private static Collection<CatalogPermission> getCatalog() {
        return GameServerState.instance.getCatalogManager().getCatalog();
    }
}
