package game.engine;

import java.util.Random;

/**
 * Dice - 骰子类
 * 提供随机掷骰子功能
 */
public class Dice {
    private static final Random random = new Random();

    public static int roll() {
        // return 6;
        return roll(6);
    }

    public static int roll(int sides) {
        if (sides <= 0) {
            throw new IllegalArgumentException("骰子面数必须大于0");
        }
        return random.nextInt(sides) + 1;
    }

    public static void setSeed(long seed) {
        random.setSeed(seed);
    }
}