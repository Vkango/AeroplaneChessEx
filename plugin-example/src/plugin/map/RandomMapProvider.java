package plugin.map;

import plugin.api.IMapProvider;
import plugin.blocks.*;
import plugin.api.Node;
import ui.api.Colors;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class RandomMapProvider implements IMapProvider {
    private Node[][] nodes = null;
    private int pathLength; // 每个玩家的路径长度
    private int playerCount; // 玩家数量
    private int gridWidth; // 网格宽度
    private int gridHeight; // 网格高度
    private Random random;
    private long seed; // 随机种子，用于可复现的地图

    // 特殊方块配置（每pathLength格子的比例）
    private static final double LUCKY_RATIO = 0.10; // 幸运格 10%
    private static final double SPEEDUP_RATIO = 0.08; // 加速格 8%
    private static final double SLOWDOWN_RATIO = 0.06; // 减速格 6%
    private static final double MINE_RATIO = 0.04; // 地雷格 4%
    private static final double TELEPORT_RATIO = 0.03; // 传送格 3%
    private static final Colors[] COLORS = { new Colors(255, 0, 0), new Colors(0, 0, 255), new Colors(128, 0, 128),
            new Colors(0, 255, 0) };
    private static final String[] COLORS_NAME = { new String("红色"), new String("蓝色"), new String("紫色"),
            new String("绿色") };

    @Override
    public Colors[] getPlayerColors() {
        return COLORS;
    }

    @Override
    public String[] getPlayerColorsName() {
        return COLORS_NAME;
    }

    @Override
    public int getMaxPlayers() {
        return 4;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    public RandomMapProvider() {
        this(4, 52, System.currentTimeMillis());
    }

    public RandomMapProvider(int playerCount, int pathLength, long seed) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("玩家数量必须在2-4之间");
        }
        if (pathLength < 20 || pathLength > 100) {
            throw new IllegalArgumentException("路径长度必须在20-100之间");
        }

        this.playerCount = playerCount;
        this.pathLength = pathLength;
        this.seed = seed;
        this.random = new Random(seed);
        calculateGridSize();
    }

    private void calculateGridSize() {
        this.gridWidth = 13;
        this.gridHeight = (int) Math.ceil((double) pathLength / gridWidth);

        System.out.println("[RandomMapProvider] 地图大小: " + gridWidth + "x" + gridHeight +
                ", 路径长度: " + pathLength + ", 玩家数: " + playerCount);
    }

    @Override
    public String getName() {
        return "Random Map Generator";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "随机地图";
    }

    @Override
    public String getAuthor() {
        return "System";
    }

    @Override
    public String getMapName() {
        return "random"; // SPI识别名称
    }

    @Override
    public int[] positionToXY(int position) {
        int row = position / gridWidth;
        int col = position % gridWidth;

        if (row % 2 == 0) {
            return new int[] { col, row };
        } else {
            return new int[] { gridWidth - 1 - col, row };
        }
    }

    @Override
    public int getMaxChessmanPerPlayer() {
        return 4;
    }

    @Override
    public Node[][] getMap() {
        if (nodes != null) {
            return nodes;
        }
        nodes = new Node[gridHeight][gridWidth];
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                nodes[i][j] = new Node(-1, new Empty());
            }
        }
        List<BlockType> blockPattern = generateBlockPattern();
        for (int pos = 0; pos < pathLength; pos++) {
            int[] xy = positionToXY(pos);
            int x = xy[0];
            int y = xy[1];

            plugin.api.IBlock block;

            if (pos == 0) {
                // 起点
                block = new Start();
            } else if (pos == pathLength - 1) {
                // 终点
                block = new Destination();
            } else {
                // 根据模板分配特殊方块
                block = createBlockFromType(blockPattern.get(pos));
            }

            nodes[y][x] = new Node(pos, block);
        }

        System.out.println("[RandomMapProvider] 地图生成完成，种子: " + seed);
        return nodes;
    }

    private List<BlockType> generateBlockPattern() {
        List<BlockType> pattern = new ArrayList<>();
        int luckyCount = (int) (pathLength * LUCKY_RATIO);
        int speedUpCount = (int) (pathLength * SPEEDUP_RATIO);
        int slowDownCount = (int) (pathLength * SLOWDOWN_RATIO);
        int mineCount = (int) (pathLength * MINE_RATIO);
        int teleportCount = (int) (pathLength * TELEPORT_RATIO);
        mineCount = Math.min(mineCount, pathLength / 10);
        System.out.println("[RandomMapProvider] 特殊方块数量\n幸运:" + luckyCount +
                ", 加速:" + speedUpCount + ", 减速:" + slowDownCount +
                ", 地雷:" + mineCount + ", 传送:" + teleportCount);
        for (int i = 0; i < pathLength; i++) {
            pattern.add(BlockType.NORMAL);
        }

        int safeZoneStart = 2; // 起点后2格安全
        int safeZoneEnd = pathLength - 3; // 终点前2格安全

        // 均匀分配幸运格
        distributeBlocks(pattern, BlockType.LUCKY, luckyCount, safeZoneStart, safeZoneEnd);

        // 均匀分配加速格
        distributeBlocks(pattern, BlockType.SPEEDUP, speedUpCount, safeZoneStart, safeZoneEnd);

        // 均匀分配减速格
        distributeBlocks(pattern, BlockType.SLOWDOWN, slowDownCount, safeZoneStart, safeZoneEnd);

        // 均匀分配传送格
        distributeBlocks(pattern, BlockType.TELEPORT, teleportCount, safeZoneStart, safeZoneEnd);

        // 最后分配地雷（避免连续）
        distributeMines(pattern, mineCount, safeZoneStart, safeZoneEnd);

        return pattern;
    }

    private void distributeBlocks(List<BlockType> pattern, BlockType type, int count, int start, int end) {
        if (count <= 0)
            return;

        int availableRange = end - start;
        if (availableRange <= 0)
            return;
        double interval = (double) availableRange / count;

        for (int i = 0; i < count; i++) {
            int basePos = start + (int) (i * interval);
            int randomOffset = random.nextInt((int) interval + 1);
            int pos = Math.min(basePos + randomOffset, end - 1);
            while (pos < end && pattern.get(pos) != BlockType.NORMAL) {
                pos++;
            }

            if (pos < end) {
                pattern.set(pos, type);
            }
        }
    }

    private void distributeMines(List<BlockType> pattern, int count, int start, int end) {
        if (count <= 0)
            return;

        int placed = 0;
        int attempts = 0;
        int maxAttempts = count * 10;

        while (placed < count && attempts < maxAttempts) {
            attempts++;
            int pos = start + random.nextInt(end - start);
            if (pattern.get(pos) == BlockType.NORMAL &&
                    !hasMineNearby(pattern, pos)) {
                pattern.set(pos, BlockType.MINE);
                placed++;
            }
        }

        System.out.println("[RandomMapProvider] 成功放置 " + placed + "/" + count + " 个地雷");
    }

    private boolean hasMineNearby(List<BlockType> pattern, int pos) {
        for (int i = Math.max(0, pos - 2); i <= Math.min(pattern.size() - 1, pos + 2); i++) {
            if (i != pos && pattern.get(i) == BlockType.MINE) {
                return true;
            }
        }
        return false;
    }

    private plugin.api.IBlock createBlockFromType(BlockType type) {
        switch (type) {
            case START:
                return new Start();
            case DESTINATION:
                return new Destination();
            case LUCKY:
                return new Lucky();
            case SPEEDUP:
                return new SpeedUp();
            case SLOWDOWN:
                return new SlowDown();
            case MINE:
                return new Mine();
            case TELEPORT:
                return new Teleport();
            case NORMAL:
            default:
                return new Normal();
        }
    }

    @Override
    public int getMapSize() {
        return pathLength;
    }

    @Override
    public int getDifficulty() {
        double dangerRatio = MINE_RATIO + SLOWDOWN_RATIO;
        if (dangerRatio > 0.15)
            return 4;
        if (dangerRatio > 0.10)
            return 3;
        return 2;
    }

    @Override
    public int getRecommendedPlayers() {
        return playerCount;
    }

    @Override
    public int getEndPosition(int playerIndex) {
        return pathLength - 1;
    }

    @Override
    public int getStartPosition(int playerIndex) {
        return 0;
    }

    private enum BlockType {
        START,
        DESTINATION,
        NORMAL,
        LUCKY,
        SPEEDUP,
        SLOWDOWN,
        MINE,
        TELEPORT
    }
}
