package ui.api;

public final class Colors {
    public int r;
    public int g;
    public int b;
    public static final Colors RED = new Colors(231, 76, 60);
    public static final Colors GREEN = new Colors(46, 204, 113);
    public static final Colors BLUE = new Colors(52, 152, 219);
    public static final Colors BLACK = new Colors(0, 0, 0);
    public static final Colors WHITE = new Colors(255, 255, 255);
    public static final Colors YELLOW = new Colors(241, 196, 15);
    public static final Colors PURPLE = new Colors(155, 89, 182);
    public static final Colors CYAN = new Colors(26, 188, 156);
    public static final Colors ORANGE = new Colors(230, 126, 34);
    public static final Colors GRAY = new Colors(149, 165, 166);
    public static final Colors DARK_GRAY = new Colors(127, 140, 141);

    public Colors(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}