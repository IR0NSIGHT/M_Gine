import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.09.2021
 * TIME: 20:29
 */
public class MissionDestroy extends Mission {
    Object target;
    public Object getTarget() {
        return target;
    }
    public void setTarget(Object target) {
        this.target = target;
    }

    public MissionDestroy(Random rand, long seed) {
        super(rand, seed);
    }
}
