package ui.tui;

import ui.api.IMapRenderer;
import ui.util.ColorFactory;
import game.api.*;
import plugin.api.IMapProvider;
import plugin.api.Node;

import java.util.List;

public class Renderer implements IMapRenderer {
    private int highlightedPosition = -1;
    private IGameContext lastContext = null;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public void render(IGameContext context) {
        if (context == null) {
            return;
        }

        clear();
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    飞行棋游戏");
        System.out.println("=".repeat(60));
        System.out.println("\n回合数: " + context.getCurrentTurnNumber());
        IPlayer currentPlayer = context.getCurrentPlayer();
        if (currentPlayer != null) {
            System.out.println("当前玩家: " + getColoredText(currentPlayer.getName(),
                    (String) ColorFactory.of(context.getMapProvider().getPlayerColors()[currentPlayer.getPlayerId()]))
                    + " ("
                    + currentPlayer.getPlayerId() + ")");
        }

        System.out.println();
    }

    @Override
    public void renderMap(IGameContext context) {
        if (context == null || context.getMapProvider() == null) {
            System.out.println("[ERROR] 地图未加载");
            return;
        }
        this.lastContext = context;

        IMapProvider mapProvider = context.getMapProvider();
        Node[][] map = mapProvider.getMap();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("                      游戏地图");
        System.out.println("-".repeat(60) + "\n");
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Node node = map[y][x];
                if (node.getBlock() != null && "Empty".equals(node.getBlock().getType())) {
                    System.out.print("   ");
                    continue;
                }

                int position = node.getPosition();
                List<IChessman> chessmenAtPos = context.getBoard().getChessmenAt(position);

                if (!chessmenAtPos.isEmpty()) {
                    IChessman chessman = chessmenAtPos.get(0);
                    String colorCode = (String) ColorFactory.of(
                            mapProvider.getPlayerColors()[chessman.getOwner().getPlayerId()]);
                    String chessmanSymbol = chessman.getOwner().getName().charAt(0)
                            + String.valueOf(chessman.getChessmanId());
                    System.out.print(getColoredText(chessmanSymbol, colorCode) + "   ");
                } else {
                    String blockSymbol = getBlockSymbol(node);
                    if (position == highlightedPosition) {
                        System.out.print(ANSI_BOLD + ANSI_YELLOW + blockSymbol + ANSI_RESET + " ");
                    } else {
                        System.out.print(blockSymbol + " ");
                    }
                }
            }
            System.out.println();
        }

        System.out.println("\n" + "-".repeat(60));

        renderLegend();
    }

    private void renderLegend() {
        if (lastContext == null || lastContext.getMapProvider() == null) {
            return;
        }

        System.out.println("\n图例说明:");

        java.util.Set<String> blockTypesSeen = new java.util.LinkedHashSet<>();
        java.util.Map<String, plugin.api.IBlock> blockExamples = new java.util.LinkedHashMap<>();

        IMapProvider mapProvider = lastContext.getMapProvider();
        Node[][] map = mapProvider.getMap();

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Node node = map[y][x];
                if (node.getBlock() != null) {
                    String blockType = node.getBlock().getType();
                    if (!blockTypesSeen.contains(blockType)) {
                        blockTypesSeen.add(blockType);
                        blockExamples.put(blockType, node.getBlock());
                    }
                }
            }
        }

        StringBuilder legendLine = new StringBuilder("  ");
        int itemCount = 0;

        for (java.util.Map.Entry<String, plugin.api.IBlock> entry : blockExamples.entrySet()) {
            plugin.api.IBlock block = entry.getValue();

            if ("Empty".equals(block.getType())) {
                continue;
            }

            String colorCode = (String) ColorFactory.of(block.getColor());
            String symbol = block.getTUISymbol();
            String description = block.getType();

            legendLine.append(colorCode).append(symbol).append(ANSI_RESET)
                    .append(" = ").append(description).append("    ");

            itemCount++;
            if (itemCount % 3 == 0) {
                System.out.println(legendLine.toString());
                legendLine = new StringBuilder("  ");
            }
        }

        if (legendLine.length() > 2) {
            System.out.println(legendLine.toString());
        }

        if (lastContext.getPlayers() != null && lastContext.getPlayers().length > 0) {
            System.out.println();
            StringBuilder playerLegend = new StringBuilder("  ");
            for (int i = 0; i < Math.min(2, lastContext.getPlayers().length); i++) {
                IPlayer player = lastContext.getPlayers()[i];
                if (player != null) {
                    String colorCode = (String) ColorFactory.of(
                            mapProvider.getPlayerColors()[player.getPlayerId()]);
                    String example = player.getName().charAt(0) + "0";
                    playerLegend.append(colorCode).append(ANSI_BOLD).append(example)
                            .append(ANSI_RESET).append(" = 玩家")
                            .append(player.getName()).append("的0号棋子    ");
                }
            }
            System.out.println(playerLegend.toString());
        }

        System.out.println("-".repeat(60));
    }

    public void renderPlayers(IPlayer[] players) {
        if (players == null || players.length == 0) {
            return;
        }

        System.out.println("\n" + "-".repeat(60));
        System.out.println("                    玩家状态");
        System.out.println("-".repeat(60));

        for (IPlayer player : players) {
            if (player == null)
                continue;

            IMapProvider mapProvider = null;
            if (lastContext != null) {
                mapProvider = lastContext.getMapProvider();
            }

            String colorCode = "";
            if (mapProvider != null) {
                colorCode = (String) ColorFactory.of(
                        mapProvider.getPlayerColors()[player.getPlayerId()]);
            }

            System.out.println("\n玩家: " + getColoredText(player.getName(), colorCode) +
                    (player.isWinner() ? " [获胜者]" : ""));

            IChessman[] chessmen = player.getChessman();

            for (int i = 0; i < chessmen.length; i++) {
                IChessman chessman = chessmen[i];
                int position = chessman.getPosition();
                String status = "";

                if (position == -1) {
                    status = "待起飞";
                } else if (mapProvider != null && position == mapProvider.getEndPosition(player.getPlayerId())) {
                    status = "已到达终点";
                } else {
                    status = "位置 " + position;
                }
                System.out.println("  棋子 " + i + ": " + status);
            }
        }

        System.out.println("-".repeat(60));
    }

    public void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                for (int i = 0; i < 2; i++) {
                    System.out.println();
                }
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 2; i++) {
                System.out.println();
            }
        }
    }

    public void refresh() {
        System.out.flush();
    }

    private String getBlockSymbol(Node node) {
        if (node == null || node.getBlock() == null) {
            return "··";
        }

        plugin.api.IBlock block = node.getBlock();
        String symbol = block.getTUISymbol();
        String colorCode = (String) ColorFactory.of(block.getColor());

        return colorCode + symbol + ANSI_RESET;
    }

    private String getColoredText(String text, String colorCode) {
        return colorCode + ANSI_BOLD + text + ANSI_RESET;
    }
}
