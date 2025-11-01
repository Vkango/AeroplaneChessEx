package ui.util;

import ui.api.Colors;

public enum ColorFactory {
    GUI,
    TUI;

    private static ColorFactory current = GUI;

    public static void set(ColorFactory mode) {
        current = mode;
    }

    public static Object of(Colors color) {
        return switch (current) {
            case GUI -> javafx.scene.paint.Color.rgb(color.r, color.g, color.b);
            case TUI -> String.format("\033[38;2;%d;%d;%dm", color.r, color.g, color.b);
        };
    }
}