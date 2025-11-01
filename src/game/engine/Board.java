package game.engine;

import java.util.List;

import game.api.*;
import plugin.api.IMapProvider;
import plugin.api.Node;

import java.util.ArrayList;

public class Board implements IBoard {
    private final GameContext context = GameContext.getInstance();

    // 用于防止方块效果无限递归
    private ThreadLocal<Integer> blockEffectDepth = ThreadLocal.withInitial(() -> 0);

    @Override
    public boolean inDestination(IMapProvider mapProvider, IChessman chessman, int position) {
        if (mapProvider == null || chessman == null) {
            return false;
        }

        return position == mapProvider.getEndPosition(chessman.getOwner().getPlayerId());
    }

    @Override
    public boolean inStart(IChessman chessman, int position) {
        IMapProvider mapProvider = context.getMapProvider();
        if (mapProvider == null || chessman == null) {
            return false;
        }

        int startPosition = mapProvider.getStartPosition(chessman.getOwner().getPlayerId());
        return position == startPosition;
    }

    @Override
    public List<IChessman> getChessmenAt(int position) {
        List<IChessman> result = new ArrayList<>();
        IPlayer[] players = context.getPlayers();
        if (players != null) {
            for (IPlayer player : players) {
                for (IChessman chessman : player.getChessman()) {
                    if (chessman.getPosition() == position) {
                        result.add(chessman);
                    }
                }
            }
        }
        return result;
    }

    private void onChessmanMoved(IGameEvent event) {
        // 表示已经移动完成到了一个格子的事件。
        // 步数为0，就重新触发当前方块事件。
        if (event == null || !(event.getData() instanceof IChessman)) {
            return;
        }

        IChessman chessman = (IChessman) event.getData();

        // 检查递归深度
        int depth = blockEffectDepth.get();
        if (depth >= context.getRuleSetProvider().getMaxBlockEffectDepth()) {
            java.util.Map<String, Object> effectData = new java.util.HashMap<>();
            effectData.put("chessman", chessman);
            effectData.put("message", "[Board] 以达到最高连锁反应次数，效果停止触发！");
            EventBus.getInstance().publish(new GameEvent("BlockEffect", effectData, "方块效果触发"));
            System.err.println("[Board] 警告：方块效果递归深度过大，停止处理");
            return;
        }

        try {
            blockEffectDepth.set(depth + 1);
            int position = chessman.getPosition();
            if (context.getMapProvider() == null) {
                return;
            }
            int[] xy = context.getMapProvider().positionToXY(position);
            if (xy[0] < 0 || xy[1] < 0) {
                return;
            }

            Node node = context.getMapProvider().getMap()[xy[1]][xy[0]];
            // 连锁事件处理
            if (node != null && node.getBlock() != null) {
                String blockType = node.getBlock().getClass().getSimpleName();
                System.out.println("[DEBUG] 位置" + position + "的方块类型: " + blockType +
                        " (深度=" + depth + ")");
                node.getBlock().onLand(chessman, context);
            }
        } finally {
            blockEffectDepth.set(depth);
        }
    }

    public Board() {
        EventBus.getInstance().subscribe("ChessmanMoved", this::onChessmanMoved);
        EventBus.getInstance().subscribe("ChessmanTakeOff", this::onChessmanMoved);
    }

    @Override
    public boolean moveChessman(IChessman chessman, int steps) {
        if (chessman == null || steps == 0) {
            return false;
        }

        IMapProvider mapProvider = context.getMapProvider();
        if (mapProvider == null) {
            return false;
        }

        int currentPos = chessman.getPosition();
        int endPosition = mapProvider.getEndPosition(chessman.getOwner().getPlayerId());

        // 逐格移动
        if (steps > 0) {
            for (int i = 0; i < steps; i++) {
                int nextPos = currentPos + 1;

                // 检查是否会越界
                if (nextPos > endPosition) {
                    // 发布越界事件
                    java.util.Map<String, Object> overEndData = new java.util.HashMap<>();
                    overEndData.put("chessman", chessman);
                    overEndData.put("targetPosition", currentPos + (steps - i));
                    overEndData.put("endPosition", endPosition);
                    overEndData.put("overSteps", (currentPos + (steps - i)) - endPosition);
                    EventBus.getInstance().publish(new GameEvent("ChessmanOverEnd", overEndData,
                            "棋子越过终点"));
                    return false; // RuleEngine会处理怎么动，最后直接给出移动结果，不用管动画了
                }

                boolean isLastStep = (i == steps - 1);

                // 只在最后一步才发布ChessmanMoved事件，中间步骤只发布动画事件
                if (!isLastStep) {
                    // 中间步骤：只更新位置和发布动画事件
                    if (chessman instanceof Chessman) {
                        Chessman chess = (Chessman) chessman;
                        int oldPos = chess.getPosition();
                        // 直接修改内部位置，不触发事件
                        java.lang.reflect.Field posField;
                        try {
                            posField = Chessman.class.getDeclaredField("position");
                            posField.setAccessible(true);
                            posField.setInt(chess, nextPos);

                            // 只发布动画事件
                            java.util.Map<String, Object> moveData = new java.util.HashMap<>();
                            moveData.put("chessman", chess);
                            moveData.put("from", oldPos);
                            moveData.put("to", nextPos);
                            EventBus.getInstance().publish(new GameEvent("ChessmanMoveEasing", moveData,
                                    "棋子移动动画"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // 最后一步：触发完整的事件（包括方块效果）
                    chessman.setPosition(nextPos);
                }

                currentPos = nextPos;

                // UI动画延迟
                long delay = AnimationConfig.getMoveDelay();
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } else if (steps < 0) { // 处理后退
            for (int i = 0; i < Math.abs(steps); i++) {
                int nextPos = Math.max(0, currentPos - 1);

                boolean isLastStep = (i == Math.abs(steps) - 1);
                if (!isLastStep) {
                    if (chessman instanceof Chessman) {
                        Chessman chess = (Chessman) chessman;
                        int oldPos = chess.getPosition();
                        try {
                            java.lang.reflect.Field posField = Chessman.class.getDeclaredField("position");
                            posField.setAccessible(true);
                            posField.setInt(chess, nextPos);

                            java.util.Map<String, Object> moveData = new java.util.HashMap<>();
                            moveData.put("chessman", chess);
                            moveData.put("from", oldPos);
                            moveData.put("to", nextPos);
                            EventBus.getInstance().publish(new GameEvent("ChessmanMoveEasing", moveData,
                                    "棋子移动动画"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    chessman.setPosition(nextPos);
                }

                currentPos = nextPos;

                long delay = AnimationConfig.getMoveDelay();
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return true;
    }

    /**
     * 重置棋盘状态（用于重新开始游戏）
     */
    public void reset() {
        // 重置所有玩家
        IPlayer[] players = context.getPlayers();
        if (players != null) {
            for (IPlayer player : players) {
                if (player instanceof Player) {
                    ((Player) player).reset();
                }
            }
        }
    }
}
