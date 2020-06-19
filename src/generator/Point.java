package generator;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class Point implements Comparable<Point> {

    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Circle getRepresentation() {
        return new Circle(x, y, 2);
    }

    public Circle getCoverageRepresentation(double radius) {
        Circle circle = new Circle(x, y, radius);
        circle.setFill(Color.rgb(57, 143, 93, 0.2));
        return circle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) {
            return false;
        }
        if (o instanceof Point || o instanceof BorderPoint) {
            Point point = (Point) o;
            return Double.compare(point.x, x) == 0 &&
                    Double.compare(point.y, y) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Point o) {
        if (this.y == o.y) {
            if (this.x == o.x) return 0;
            else if (this.x > o.x) return 1;
            else return -1;        }
        else if (this.y > o.y) {
            return 1;
        }
        else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "[" + x + ":" + y + "]";
    }
}
