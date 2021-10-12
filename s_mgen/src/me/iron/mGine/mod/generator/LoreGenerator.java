package me.iron.mGine.mod.generator;

import me.iron.mGine.mod.missions.wrappers.DataBaseStation;
import org.apache.xmlbeans.impl.piccolo.xml.Entity;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

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

    public String generateTransportBriefing(DataBaseStation from, DataBaseStation to, String cargoName, int cargoUnits, long seed) {
        String[] excuses = new String[]{
                "Our warehouse has completely sold all its stocks of"+cargoUnits+". ",
                "A fire has destroyed our warehouse. ",
                "A group of hoppys escaped from the zoo and ate our supplies of " + cargoName+" .",
                "A skirmish with pirates forced us to abandon a crate of " + cargoName +" .",
                "Pirates intercepted a shipment of ours. ",
                "The local manager stole the complete supplies of "+cargoName+". ",
                "An intern of our accidentally threw away all "+cargoName+". "
        };
        String[] orderSs = new String[]{"Transport ", "We need you to transport ","We require a delivery of ","We need to ship a supply of "};
        String[] fromSs = new String[]{" from "," stored at ", " out of "," starting at ", " from our base ", " from station "};
        String[] toSs = new String[]{" to "," to our station"," to our base "," ,destination ", " targeting our station "," ,restocking ", " ,resupplying "};
        Random r = new Random(seed);
        String out = (r.nextBoolean())?"":getRand(excuses,r);
        out += getRand(orderSs,r) + cargoUnits + " units of " + cargoName + getRand(fromSs,r) + "'"+from.getName()+"'" + getRand(toSs,r) + ((!to.getName().equals(""))?"'"+to.getName()+"'":"")+" at " + to.getPosition().toStringPure();
        return out;
    }

    public String generateTransportName(DataBaseStation from, DataBaseStation to, String cargoName,int cargoUnits,long seed) {
        String[] transportSs = new String[]{"Transport "," Ferry "," Bring "," Move "," Ship "};
        Random random = new Random(seed);
        return getRand(transportSs,random)+cargoUnits+"x "+cargoName+" from "+from.getName()+" to "+to.getPosition().toStringPure();
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