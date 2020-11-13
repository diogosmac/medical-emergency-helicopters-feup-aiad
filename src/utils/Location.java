package utils;

public class Location implements java.io.Serializable {

    private final double x;
    private final double y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getDistance(Location that) {
        double deltaX = this.x - that.x;
        double deltaY = this.y - that.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public String toString() {
        return "Location { " +
                "x=" + x +
                ", y=" + y +
                " }";
    }

}
