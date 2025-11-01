package plugin.rule;

import plugin.api.*;
import game.api.*;

public class DefaultRuleSetProvider implements IRuleSetProvider {
    private ITakeOffCondition takeOffCondition;
    private IMoveStrategy moveStrategy;
    private IOverEndRule overEnd;

    public DefaultRuleSetProvider() {
        this.takeOffCondition = new ITakeOffCondition() {
            @Override
            public boolean canTakeOff(int diceValue) {
                return diceValue == 6;
            }

            @Override
            public String getConditionDescription() {
                return "需要投出6点才能起飞";
            }
        };

        this.overEnd = new IOverEndRule() {
            @Override
            public int handleOverEnd(IChessman chessman, int deltaSteps, IGameContext context) {
                String message = "[Rule] 棋子 " + chessman.getChessmanId() + " 超过终点，回退 "
                        + (deltaSteps) + " 步！";
                java.util.Map<String, Object> effectData = new java.util.HashMap<>();
                effectData.put("chessman", chessman);
                effectData.put("message", message);
                IGameEvent event = new IGameEvent() {
                    @Override
                    public String getType() {
                        return "BlockEffect";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T getData() {
                        return (T) effectData;
                    }

                    @Override
                    public String getDescription() {
                        return "方块效果触发";
                    }

                    @Override
                    public long getTimestamp() {
                        return System.currentTimeMillis();
                    }
                };
                context.getEventBus().publish(event);
                return -deltaSteps;
            }
        };

        this.moveStrategy = new IMoveStrategy() {
            @Override
            public boolean canMove(IChessman chessman, int steps, IGameContext context) {
                int currentPos = chessman.getPosition();
                if (chessman == null || context == null) {
                    return false;
                }

                if (currentPos < 0) {
                    return false;
                }

                int endPosition = context.getMapProvider().getEndPosition(chessman.getOwner().getPlayerId());

                if (currentPos >= endPosition) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean canCapture(IChessman attacker, IChessman defender) {
                // 允许击落对方棋子
                return attacker != null && defender != null &&
                        attacker.getOwner() != defender.getOwner();
            }
        };

    }

    @Override
    public IOverEndRule getOverEndRule() {
        return this.overEnd;
    }

    @Override
    public String getName() {
        return "Default RuleSet";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Standard Aeroplane Chess Rules";
    }

    @Override
    public int getMaxBlockEffectDepth() {
        return 5; // 为1禁止连锁反应
    }

    @Override
    public String getAuthor() {
        return "System";
    }

    @Override
    public String getRuleSetName() {
        return "default"; // SPI识别名称
    }

    @Override
    public ITakeOffCondition getTakeOffCondition() {
        return this.takeOffCondition;
    }

    @Override
    public IMoveStrategy getMoveStrategy() {
        return this.moveStrategy;
    }

    @Override
    public boolean isGameOver(IPlayer[] players) {
        if (players == null) {
            return false;
        }

        for (IPlayer player : players) {
            if (player != null && player.isWinner()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public IPlayer getWinner(IPlayer[] players) {
        if (players == null) {
            return null;
        }

        for (IPlayer player : players) {
            if (player != null && player.isWinner()) {
                return player;
            }
        }

        return null;
    }

    @Override
    public boolean shouldGrantExtraTurn(int diceValue) {
        return diceValue == 6;
    }
}
