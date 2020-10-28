package utils;

public class Location {

    private double x;
    private double y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getDistance(Location that) {
        double deltaX = this.x - that.x;
        double deltaY = this.y - that.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
