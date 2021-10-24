package me.iron.mGine.mod.generator;

import me.iron.mGine.mod.missions.MissionUtil;
import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import org.apache.xmlbeans.impl.piccolo.xml.Entity;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.util.List;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.10.2021
 * TIME: 11:03
 * builds mission briefings that wrap relevant info in interesting stories.
 */
public class LoreGenerator {
    public static LoreGenerator instance;

    public LoreGenerator() {
        instance = this;
    }

    public String enemySpottedNearby(int factionID, long seed) {
        Random rand = new Random(seed);
        List<Faction> enemies = GameServerState.instance.getFactionManager().getFaction(factionID).getEnemies();
        Faction enemyF = null;
        for (Faction f: enemies) {
            if (f.getName().toLowerCase().contains("enemy")) {
                continue;
            }
            enemyF = f;
        }
        assert enemyF != null;
        String enemy = enemyF.getName();

        String[] spotter = {"A faring trader","A spy satellite","An intelligence service source","A patrol"};
        String[] faction = {"pirate","scavenger","dutch"};
        String[] location = {"in our system","near our territory","in the asteroid belt nearby","at yo mama's ass"};
        String[] spotted = {"spotted","reported","seen","identified","made out"};
        String[] causes = new String[]{
                "$spotter$ has $spotted$ $faction$ ships $location$"
        };

        String text = getRand(causes,rand);
        text = text.replace("$spotter$",getRand(spotter,rand));
        text = text.replace("$faction$",enemy);
        text = text.replace("$location$",getRand(location,rand)+".");
        text = text.replace("$spotted$",getRand(spotted,rand));
        return text;
    }

    public String generateTransportBriefing(DataBaseStation from, DataBaseStation to, String cargoName, int cargoUnits, long seed) {
        String[] excuses = new String[]{
                "Our warehouse has completely sold all its stocks of "+cargoName+", we need to get more ASAP! Otherwise we'll loose out on a huge sum of money.",
                "A fire has destroyed our warehouse, and its vital that we restock so the customers dont get angry.",
                "A group of hoppys escaped from the zoo and ate our complete supply of " + cargoName+" . The police has caught them by now, but the damage is done.",
                "A skirmish with pirates forced us to abandon a crate of " + cargoName +" . We tried to recover it, but its gone. Probably already sold on the black market.",
                "Pirates intercepted a shipment of ours. And what does our navy do? Getting drunk in the ports as usual, while we loose lives and cargo.",
                "The local manager stole the complete supplies of "+cargoName+". I hope he chokes on it, i was supposed to retire tomorrow, and now this mess.",
                "An intern accidentally threw away all "+cargoName+". Sadly he's the bosses wife's affair's cousin, so theres nothing we can do about it.."
        };
        String[] orderSs = new String[]{"Transport ", "We need you to transport ","We require a delivery of ","We need to ship a supply of "};
        String[] fromSs = new String[]{" from "," stored at ", " out of "," starting at ", " from our base ", " from station "};
        String[] toSs = new String[]{" to "," to our station"," to our base "," ,destination ", " targeting our station "," ,restocking ", " ,resupplying "};
        Random r = new Random(seed);
        String out = (r.nextBoolean())?"":getRand(excuses,r);
        out += getRand(orderSs,r) + MissionUtil.formatMoney(cargoUnits) + " units of " + cargoName + getRand(fromSs,r) + "'"+from.getName()+"'" + getRand(toSs,r) + ((!to.getName().equals(""))?"'"+to.getName()+"'":"")+" at " + to.getPosition().toStringPure();
        return out;
    }

    public String generateTransportName(DataBaseStation from, DataBaseStation to, String cargoName,int cargoUnits,long seed) {
        String[] transportSs = new String[]{"Transport "," Ferry "," Bring "," Move "," Ship "};
        Random random = new Random(seed);
        return getRand(transportSs,random)+ MissionUtil.formatMoney(cargoUnits)+"x "+cargoName+" from "+from.getName()+" ("+from.getPosition().toStringPure() +") to "+ to.getName()+" (" +to.getPosition().toStringPure()+")";
    }

    public String generateAttackStory(String enemyFactionName, String entity,long seed) {
        Random r = new Random(seed);
        String[] bases =  {
                "This $faction$ $entity$ has $often$ $raided$ our $homes$.",
                "$often$ has this $entity$ $raided$ our $homes$"
        };
        String base = getRand(bases,r);

        String[] raided = {"launched attacks against","threatened","sent their savages against","waged war against"};
        String[] often = {"for to long","long enough","for the last time","to often","to many times"};
        String[] homes = {"homes","trading routes","stations","ships"};
        base = base.replace("$entity$",entity);
        base = base.replace("$faction$",enemyFactionName);
        base = base.replace("$raided$",getRand(raided,r));
        base = base.replace("$often$",getRand(often,r));
        base = base.replace("$homes$",getRand(homes,r));
        return base;
    }

    public String generateKillEntity(SimpleTransformableSendableObject.EntityType type, long seed) {
        Random r = new Random(seed);
        String base = "$destroy$ this $type$$extra$.";
        String[] destroy = new String[]{"Destroy","Kill","Annihilate","Eradicate","Wipe out","Obliterate","Purge"};
        String[] extra = new String[]{" once and for all"," for the glory of Morg"," to revenge the fallen",""};
        base = base.replace("$destroy$",getRand(destroy,r));
        base = base.replace("$extra$",getRand(extra,r));
        base = base.replace("$type$",type.getName());
        return base;
    }

    private String getRand(String[] arr, Random random) {
        return arr[random.nextInt(arr.length)];
    }

    public static void main(String[] args) {
        DataBaseStation start = new DataBaseStation("null","traders home",new Vector3i(4,4,4),-10000000, SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbTypeId);
        DataBaseStation end = new DataBaseStation("null","bang prime",new Vector3i(5,10,100),-10000000, SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbTypeId);
        String cargoName = "toilet paper";
        int units = 150;
        LoreGenerator g = new LoreGenerator();
        int amount = 10;
        Random random = new Random(420);
        for (int i = 0; i < amount; i++) {
            System.out.println(g.generateTransportBriefing(start,end,cargoName,units, random.nextLong())+"\n\n");
        }
    }
}
