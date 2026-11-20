package co.edu.unbosque.advancewars.util;

public class RoadNode implements Comparable<RoadNode> {

    private final int x;
    private final int y;
    private final int direction;
    private final int cost;
    private final RoadNode parent;

    public RoadNode(int x, int y, int direction, int cost, RoadNode parent) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.cost = cost;
        this.parent = parent;
    }

    @Override
    public int compareTo(RoadNode other) {
        return Integer.compare(this.cost, other.cost);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDirection() {
        return direction;
    }

    public int getCost() {
        return cost;
    }

    public RoadNode getParent() {
        return parent;
    }
}