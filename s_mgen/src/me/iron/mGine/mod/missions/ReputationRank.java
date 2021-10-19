package me.iron.mGine.mod.missions;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.10.2021
 * TIME: 16:27
 */
public enum ReputationRank {
    ENEMY_OF_THE_STATE(-5000, "enemy of the state"), //-4
    INSURGENT(-4000, "insurgent"),
    OUTCAST(-3000, "outcast"),
    VAGABOND(-2000, "vagabond"),
    NEUTRAL(-1000, "neutral"), //1
    ASSOCIATE(1000, "associate"), //8
    ALLY(2000, "ally"),   //64
    PROTECTOR(3000, "protector"), //512
    HERO(4000, "hero"); //4096
    private int points;
    private String name;

    ReputationRank(int points, String name) {
        this.points = points;
        this.name = name;
    }
    private static int maxPoints = 4096; //points at which max level is reached.
    private static int minPoints = -4096; //minimum level points
    public static ReputationRank getReputation(int diplomacyPoints) {
        for (int i = 0; i < values().length - 1; i++) {
            System.out.println("dip points: " + diplomacyPoints + " vs lvl ps " + values()[i + 1].points);
            int idxPoints = values()[i + 1].points + Math.abs(minPoints);
            if (diplomacyPoints + Math.abs(minPoints) < idxPoints)
                return values()[i];
        }
        return HERO;
    }

    public int getPoints() {
        return points;
    }
}
