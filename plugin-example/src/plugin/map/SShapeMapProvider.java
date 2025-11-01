package plugin.map;

import plugin.api.IMapProvider;
import plugin.api.Node;
import ui.api.Colors;
import plugin.blocks.*;

public class SShapeMapProvider implements IMapProvider {
    private Node[][] map;
    private int mapSize;
    private static final int WIDTH = 13;
    private static final int HEIGHT = 7;
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

    public SShapeMapProvider() {
        initMap();
    }

    private void initMap() {
        map = new Node[HEIGHT][WIDTH];

        // 初始化所有位置为 Empty
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                map[y][x] = new Node(-1, new Empty());
            }
        }

        for (int pos = 0; pos <= 24; pos++) {
            int[] xy = positionToXY(pos);
            if (xy[0] < 0 || xy[1] < 0 || xy[0] >= WIDTH || xy[1] >= HEIGHT) {
                System.err.println("[ERROR] 位置 " + pos + " 映射到无效坐标: [" + xy[0] + ", " + xy[1] + "]");
                continue;
            }
            int x = xy[0];
            int y = xy[1];

            plugin.api.IBlock block;
            if (pos == 0) {
                block = new Start();
            } else if (pos == 24) {
                block = new Destination();
            } else if (pos >= 17) {
                block = new Lucky();
            } else if (pos == 5) {
                block = new Mine();
            } else if (pos == 8) {
                block = new SpeedUp();
            } else {
                block = new Normal();
            }

            map[y][x] = new Node(pos, block);
        }

        // 玩家2路线 (25-49): 25个位置
        for (int pos = 25; pos <= 49; pos++) {
            int[] xy = positionToXY(pos);
            if (xy[0] < 0 || xy[1] < 0 || xy[0] >= WIDTH || xy[1] >= HEIGHT) {
                System.err.println("[ERROR] 位置 " + pos + " 映射到无效坐标: [" + xy[0] + ", " + xy[1] + "]");
                continue;
            }
            int x = xy[0];
            int y = xy[1];

            plugin.api.IBlock block;
            if (pos == 25) {
                block = new Start();
            } else if (pos == 49) {
                block = new Destination();
            } else if (pos == 28) {
                block = new Mine();
            } else if (pos >= 32) {
                block = new Lucky();
            } else if (pos == 41) {
                block = new Lucky();
            } else {
                block = new Normal();
            }

            map[y][x] = new Node(pos, block);
        }

        mapSize = 50;
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public String getMapName() {
        return "default"; // SPI识别名称
    }

    @Override
    public Node[][] getMap() {
        return map;
    }

    @Override
    public int getMapSize() {
        return mapSize;
    }

    @Override
    public int getDifficulty() {
        return 2;
    }

    @Override
    public int getRecommendedPlayers() {
        return 2;
    }

    @Override
    public int getMaxChessmanPerPlayer() {
        return 4; // 每个玩家最多4个棋子
    }

    @Override
    public int[] positionToXY(int position) {
        if (position >= 0 && position <= 24) {
            if (position <= 10) {
                return new int[] { position, 0 };
            } else if (position == 11) {
                return new int[] { 10, 1 };
            } else if (position <= 22) {
                int offset = position - 12; // 0~10
                return new int[] { 10 - offset, 2 };
            } else if (position == 23) {
                return new int[] { 0, 3 };
            } else {
                return new int[] { 1, 4 };
            }
        }

        if (position >= 25 && position <= 49) {
            if (position == 25) {
                return new int[] { 11, 4 };
            } else if (position <= 36) {
                int offset = position - 26;
                return new int[] { 11 - offset, 5 };
            } else if (position == 37) {
                return new int[] { 1, 6 };
            } else if (position <= 48) {
                int offset = position - 38;
                return new int[] { 2 + offset, 6 };
            } else {
                return new int[] { 12, 5 };
            }
        }

        return new int[] { -1, -1 };
    }

    @Override
    public int getStartPosition(int playerIndex) {
        switch (playerIndex) {
            case 0:
                return 0;
            case 1:
                return 25;
            default:
                return 0;
        }
    }

    @Override
    public int getEndPosition(int playerIndex) {
        switch (playerIndex) {
            case 0:
                return 24;
            case 1:
                return 49;
            default:
                return mapSize - 1;
        }
    }

    @Override
    public String getName() {
        return getMapName();
    }

    @Override
    public String getDescription() {
        return "双玩家独立路线的S形地图，玩家1在上半区，玩家2在下半区，路线互不干扰";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getAuthor() {
        return "System";
    }
}
