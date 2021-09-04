import com.sun.javafx.geom.Vec3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.09.2021
 * TIME: 18:10
 */
public class main {
    static Vec3f playerPos = new Vec3f();
    public static void main(String[] args) throws IOException {
        Mission m = generateMission(420);
        System.out.println(m.description);

        System.out.println("STARTING GAMELOOP");

        startGameLoop(m);
    }

    public static Vec3f getPlayerPos() {
        return playerPos;
    }

    public static float getDistance(Vec3f A, Vec3f B) {
        Vec3f off = new Vec3f(B);
        off.sub(A);
        return off.length();
    }

    public static void startGameLoop(Mission m) throws IOException {
        String in = "";
        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        int t = 0;
        m.start(0);
        while (!in.equals("stop")) {
            m.update(++t);
            System.out.println(m.getDescription());
            //let player enter his position at "x,y,z"
            in = reader.readLine();
            try {
                String[] args = in.split(",");
                int[] ints = new int[3];
                for (int i = 0; i < 3; i++) {
                    ints[i] = Integer.parseInt(args[i]);
                }
                if (args.length==3) {
                    playerPos.set(ints[0],ints[1],ints[2]);
                }

                System.out.println("player at pos: " + playerPos);
            } catch (Exception e) {

            }
        }

    }

    public static Mission generateMission(long seed) {
        Random rand = new Random(seed);
        //select random type
        MissionType type = MissionType.getByIdx(rand.nextInt());
        //generate that type
        Mission m = type.generate(rand, seed);
        return m;
    }
}
