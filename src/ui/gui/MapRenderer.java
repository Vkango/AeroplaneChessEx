package ui.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import game.api.*;
import ui.api.IMapRenderer;
import ui.util.ColorFactory;
import plugin.api.IMapProvider;
import plugin.api.Node;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MapRenderer implements IMapRenderer {
    private Canvas canvas;
    private GraphicsContext gc;

    private static final int CELL_SIZE = 60;
    private static final int PADDING = 20;
    private Map<String, ChessmanAnimation> animations = new HashMap<>();
    private AnimationTimer animationTimer;
    private IGameContext currentContext;
    private Node[][] currentMap;

    public MapRenderer() {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        gc.setFont(new Font("Microsoft YaHei", 12));
        gc.setTextAlign(TextAlignment.CENTER);

        setupAnimationTimer();
    }

    private void setupAnimationTimer() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean hasActiveAnimations = false;

                for (ChessmanAnimation anim : animations.values()) {
                    if (anim.isActive()) {
                        anim.update(now);
                        hasActiveAnimations = true;
                    }
                }

                if (hasActiveAnimations && currentContext != null) {
                    renderWithAnimations();
                }
            }
        };
        animationTimer.start();
    }

    private static class ChessmanAnimation {
        int fromPos;
        int toPos;
        List<Integer> path; // 移动路径
        int currentPathIndex = 0;
        long startTime;
        long lastStepTime;
        boolean active;

        static final long STEP_DURATION = 200_000_000L; // 200ms per step (纳秒)

        ChessmanAnimation(int from, int to, List<Integer> movePath) {
            this.fromPos = from;
            this.toPos = to;
            this.path = movePath;
            this.currentPathIndex = 0;
            this.active = true;
        }

        void start(long now) {
            this.startTime = now;
            this.lastStepTime = now;
        }

        void update(long now) {
            if (!active || path == null || path.isEmpty()) {
                active = false;
                return;
            }

            if (startTime == 0) {
                start(now);
            }

            // 检查是否该移动到下一步
            if (now - lastStepTime >= STEP_DURATION) {
                currentPathIndex++;
                lastStepTime = now;

                if (currentPathIndex >= path.size()) {
                    active = false;
                }
            }
        }

        int getCurrentPosition() {
            if (path == null || path.isEmpty() || currentPathIndex >= path.size()) {
                return toPos;
            }
            return path.get(currentPathIndex);
        }

        boolean isActive() {
            return active;
        }
    }

    @Override
    public void renderMap(IGameContext context) {
        this.currentContext = context;

        if (currentContext == null || currentContext.getMapProvider() == null) {
            clearCanvas();
            drawErrorMessage("地图未加载");
            return;
        }

        IMapProvider mapProvider = context.getMapProvider();
        Node[][] map = mapProvider.getMap();
        this.currentMap = map;

        if (map == null || map.length == 0) {
            clearCanvas();
            drawErrorMessage("地图数据为空");
            return;
        }

        int canvasWidth = map[0].length * CELL_SIZE + PADDING * 2;
        int canvasHeight = map.length * CELL_SIZE + PADDING * 2;
        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);

        clearCanvas();

        drawGrid(map, currentContext);

        drawChessmen(map, currentContext);
    }

    private void renderWithAnimations() {
        if (currentContext == null || currentMap == null) {
            return;
        }
        clearCanvas();
        drawGrid(currentMap, currentContext);
        drawChessmenWithAnimation(currentMap, currentContext);
    }

    /**
     * 启动棋子移动动画
     * 
     * @param chessmanId 棋子唯一标识符
     * @param fromPos    起始位置
     * @param toPos      目标位置
     * @param path       移动路径（经过的所有位置）
     */
    public void animateChessmanMove(String chessmanId, int fromPos, int toPos, List<Integer> path) {
        Platform.runLater(() -> {
            ChessmanAnimation anim = new ChessmanAnimation(fromPos, toPos, path);
            animations.put(chessmanId, anim);
        });
    }

    private void drawChessmenWithAnimation(Node[][] map, IGameContext context) {
        Map<Integer, List<IChessman>> positionMap = new HashMap<>();

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Node node = map[y][x];
                int position = node.getPosition();

                if (position < 0)
                    continue;

                List<IChessman> chessmenAtPos = context.getBoard().getChessmenAt(position);

                for (IChessman chessman : chessmenAtPos) {
                    String chessmanId = getChessmanId(chessman);
                    ChessmanAnimation anim = animations.get(chessmanId);

                    int renderPos = position;
                    if (anim != null && anim.isActive()) {
                        renderPos = anim.getCurrentPosition();
                    }

                    positionMap.computeIfAbsent(renderPos, k -> new java.util.ArrayList<>()).add(chessman);
                }
            }
        }

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Node node = map[y][x];
                int position = node.getPosition();

                if (position < 0)
                    continue;

                List<IChessman> chessmen = positionMap.get(position);
                if (chessmen != null && !chessmen.isEmpty()) {
                    drawChessmenAtCell(x, y, chessmen);
                }
            }
        }
    }

    private String getChessmanId(IChessman chessman) {
        return chessman.getOwner().getName() + "_" + chessman.getChessmanId();
    }

    private void clearCanvas() {
        gc.setFill(Color.rgb(236, 240, 241));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawGrid(Node[][] map, IGameContext context) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Node node = map[y][x];

                if (node.getBlock() != null && "Empty".equals(node.getBlock().getType())) {
                    continue;
                }

                drawCell(x, y, node);
            }
        }
    }

    private void drawCell(int x, int y, Node node) {
        double cellX = PADDING + x * CELL_SIZE;
        double cellY = PADDING + y * CELL_SIZE;

        Color fillColor = getCellColor(node);
        Color borderColor = Color.rgb(189, 195, 199);

        gc.setFill(fillColor);
        gc.fillRoundRect(cellX, cellY, CELL_SIZE - 2, CELL_SIZE - 2, 10, 10);

        gc.setStroke(borderColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(cellX, cellY, CELL_SIZE - 2, CELL_SIZE - 2, 10, 10);

        String cellText = getCellText(node);
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Microsoft YaHei", 11));
        gc.fillText(cellText, cellX + CELL_SIZE / 2, cellY + CELL_SIZE / 2 - 5);

        gc.setFill(Color.rgb(100, 100, 100));
        gc.setFont(new Font("Arial", 9));
        gc.fillText(String.valueOf(node.getPosition()), cellX + CELL_SIZE / 2, cellY + CELL_SIZE - 10);
    }

    private void drawChessmen(Node[][] map, IGameContext context) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Node node = map[y][x];
                int position = node.getPosition();
                if (position < 0) {
                    continue;
                }

                List<IChessman> chessmenAtPos = context.getBoard().getChessmenAt(position);
                if (!chessmenAtPos.isEmpty()) {
                    drawChessmenAtCell(x, y, chessmenAtPos);
                }
            }
        }
    }

    private void drawChessmenAtCell(int x, int y, List<IChessman> chessmen) {
        double cellX = PADDING + x * CELL_SIZE;
        double cellY = PADDING + y * CELL_SIZE;

        int count = chessmen.size();
        double radius = 12 / ((count / 4) + 1); // 棋子半径

        if (count == 1) {
            IChessman chessman = chessmen.get(0);
            drawChessman(cellX + CELL_SIZE / 2, cellY + CELL_SIZE / 2, chessman, radius);
        } else {
            double[][] offsets = {
                    { -radius, -radius },
                    { radius, -radius },
                    { -radius, radius },
                    { radius, radius }
            };

            for (int i = 0; i < count; i++) {
                IChessman chessman = chessmen.get(i);
                double cx = cellX + CELL_SIZE / 2 + offsets[i][0];
                double cy = cellY + CELL_SIZE / 2 + offsets[i][1];
                drawChessman(cx, cy, chessman, radius * 0.8);
            }
        }
    }

    private void drawChessman(double cx, double cy, IChessman chessman, double radius) {
        Color chessmanColor = (Color) ColorFactory
                .of(currentContext.getMapProvider().getPlayerColors()[chessman.getOwner().getPlayerId()]);
        gc.setFill(chessmanColor);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 10));
        String label = chessman.getOwner().getName().charAt(0) + "" + chessman.getChessmanId();
        gc.fillText(label, cx, cy + 4);
    }

    private Color getCellColor(Node node) {
        if (node == null || node.getBlock() == null) {
            return Color.rgb(149, 165, 166);
        }
        return (javafx.scene.paint.Color) ColorFactory.of(node.getBlock().getColor());
    }

    private String getCellText(Node node) {
        if (node == null || node.getBlock() == null) {
            return "?";
        }

        return node.getBlock().getType();
    }

    private void drawErrorMessage(String message) {
        gc.setFill(Color.RED);
        gc.setFont(new Font("Microsoft YaHei", 16));
        gc.fillText(message, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
