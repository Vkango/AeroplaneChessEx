package plugin.api;

public class Node {
    private int position;
    private IBlock block;
    private int value;

    public Node(int position, IBlock block) {
        this.position = position;
        this.block = block;
        this.value = 0;
    }

    public Node(int position, IBlock block, int value) {
        this.position = position;
        this.block = block;
        this.value = value;
    }

    // Getters and Setters
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public IBlock getBlock() {
        return block;
    }

    public void setBlock(IBlock block) {
        this.block = block;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Node{position=" + position + ", block=" + block.getClass().getSimpleName() + ", value=" + value + "}";
    }
}
