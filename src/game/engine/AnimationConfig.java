package game.engine;

public class AnimationConfig {
    private static long moveDelayMs = 100; // 默认延迟100ms

    public static void setMoveDelay(long delayMs) {
        moveDelayMs = delayMs;
    }

    public static long getMoveDelay() {
        return moveDelayMs;
    }
}
