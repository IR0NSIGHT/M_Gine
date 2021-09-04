package me.iron.mGine.mod.missions;


import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.game.common.data.player.PlayerState;

import javax.vecmath.Vector3f;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.09.2021
 * TIME: 14:10
 */
public class MissionTransportGoods extends Mission {
    public Vector3f getStartPos() {
        return new Vector3f(startPos);
    }

    public Vector3f getEndPos() {
        return new Vector3f(endPos);
    }

    private Vector3f startPos;
    private Vector3f endPos;
    int cargoAmount;
    float completionRadius;

    public MissionTransportGoods(Random rand, long seed) {
        super(rand,seed);
        startPos = new Vector3f(100,0,0); //new Vec3f(rand.nextInt()%100,rand.nextInt()%100,rand.nextInt()%100);
        endPos = new Vector3f(200,0,0);//new Vec3f(rand.nextInt()%100,rand.nextInt()%100,rand.nextInt()%100);
        cargoAmount = 20 + Math.abs(rand.nextInt())%80;
        completionRadius = 0.5f;
        description = "Transport cargo";
        MissionTask[] tasks = new MissionTask[3];

        //pickup stuff,
        MissionTask pickUp = new MissionTask(this,"pickup","pick up goods at "+ startPos) {
            boolean visited;

            @Override
            public void update() {
       //         visited = (visited|| mGineCore.getDistance(mGineCore.getPlayerPos(),startPos)< 0.5f);
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
        //        visited = (visited|| mGineCore.getDistance(mGineCore.getPlayerPos(),endPos)< 0.5f);
                super.update();
            }

            @Override
            protected boolean successCondition() {
                return visited;
            }
        };
        final Vector3f grandma = new Vector3f(0,100,0);
        MissionTask optional = new MissionTask(this,"grandma","visit grandma for 3 minutes at " + grandma,true) {
            int timeVisited = 0;
            @Override
            protected boolean successCondition() {
                return (timeVisited > 3);
            }

            @Override
            public void update() {
       //        if (currentState == MissionState.SUCCESS || mGineCore.getDistance(mGineCore.getPlayerPos(),grandma)< 0.5f) {
       //            timeVisited ++;
       //        }
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
        PlayerState p;
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
