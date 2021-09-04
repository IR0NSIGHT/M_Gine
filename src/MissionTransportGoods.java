import com.sun.javafx.geom.Vec3f;

import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.09.2021
 * TIME: 14:10
 */
public class MissionTransportGoods extends Mission {
    public Vec3f getStartPos() {
        return new Vec3f(startPos);
    }

    public Vec3f getEndPos() {
        return new Vec3f(endPos);
    }

    private Vec3f startPos;
    private Vec3f endPos;
    int cargoAmount;
    float completionRadius;

    public MissionTransportGoods(Random rand, long seed) {
        super(rand,seed);
        startPos = new Vec3f(100,0,0); //new Vec3f(rand.nextInt()%100,rand.nextInt()%100,rand.nextInt()%100);
        endPos = new Vec3f(200,0,0);//new Vec3f(rand.nextInt()%100,rand.nextInt()%100,rand.nextInt()%100);
        cargoAmount = 20 + Math.abs(rand.nextInt())%80;
        completionRadius = 0.5f;
        description = "Transport cargo";
        MissionTask[] tasks = new MissionTask[3];

        //pickup stuff,
        MissionTask pickUp = new MissionTask(this,"pickup","pick up goods at "+ startPos) {
            boolean visited;

            @Override
            public void update() {
                visited = (visited||main.getDistance(main.getPlayerPos(),startPos)< 0.5f);
                super.update();
            }

            @Override
            protected boolean successCondition() {
                return visited;
            }
        };
        //deliver stuff
        MissionTask deliver = new MissionTask(this,"deliver","deliver goods to " + endPos) {
            boolean visited;

            @Override
            public void update() {
                visited = (visited||main.getDistance(main.getPlayerPos(),endPos)< 0.5f);
                super.update();
            }

            @Override
            protected boolean successCondition() {
                return visited;
            }
        };
        final Vec3f grandma = new Vec3f(0,100,0);
        MissionTask optional = new MissionTask(this,"grandma","visit grandma for 3 minutes at " + grandma,true) {
            int timeVisited = 0;
            @Override
            protected boolean successCondition() {
                return (timeVisited > 3);
            }

            @Override
            public void update() {
                if (currentState == MissionState.SUCCESS || main.getDistance(main.getPlayerPos(),grandma)< 0.5f) {
                    timeVisited ++;
                }
                super.update();
            }
        };

        tasks[0] = pickUp;
        tasks[1] = deliver; deliver.setPreconditions(new MissionTask[]{pickUp});
        tasks[2] = optional;
        this.setMissionTasks(tasks);
    }

    @Override
    public void update(long time) {
        super.update(time);
    }

    @Override
    public String toString() {
        return "MissionTransportGoods{" +"\n"+
                "startPos=" + startPos +"\n"+
                ", endPos=" + endPos +"\n"+
                ", cargoAmount=" + cargoAmount +"\n"+
                ", completionRadius=" + completionRadius +"\n"+
                ", type=" + type +"\n"+
                ", duration=" + duration +"\n"+
                ", rewardCredits=" + rewardCredits +"\n"+
                ", seed=" + seed +"\n"+
                ", startTime=" + startTime +"\n"+
                ", state=" + state +"\n"+
                ", remainingTime=" + remainingTime +"\n"+
                '}';
    }
}
