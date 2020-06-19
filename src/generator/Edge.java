package generator;

import javafx.scene.shape.Line;

import java.util.Objects;

public class Edge {

    private Point startingPoint;
    private Point endingPoint;
    // Same edge but in this opposite direction
    private Edge neighbor;

    private Point leftSite;
    private Point rightSite;

    private double slope;
    private double yint;

    public Edge(Point startingPoint, Point endingPoint, Point leftSite, Point rightSite) {
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.leftSite = leftSite;
        this.rightSite = rightSite;
    }

    public Edge (Point first, Point left, Point right) {
        startingPoint = first;
        endingPoint = null;
        leftSite = left;
        rightSite = right;
        slope = (right.getX() - left.getX())/(left.getY() - right.getY());
        Point mid = new Point ((right.getX() + left.getX())/2, (left.getY()+right.getY())/2);
        yint = mid.getY() - slope*mid.getX();
    }

    public Point getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    public Point getEndingPoint() {
        return endingPoint;
    }

    public void setEndingPoint(Point endingPoint) {
        this.endingPoint = endingPoint;
    }

    public Edge getNeighbor() {
        return neighbor;
    }

    public void setNeighbor(Edge neighbor) {
        this.neighbor = neighbor;
    }

    public double getSlope() {
        return slope;
    }

    public double getYint() {
        return yint;
    }

    public Point getLeftSite() {
        return leftSite;
    }

    public Point getRightSite() {
        return rightSite;
    }

    public Line getRepresentation() {
        Line line = new Line();
        line.setStartX(this.getStartingPoint().getX());
        line.setStartY(this.getStartingPoint().getY());
        line.setEndX(this.getEndingPoint().getX());
        line.setEndY(this.getEndingPoint().getY());

        return line;
    }


    @Override
    public String toString() {
        return "Edge{" +
                "startingPoint=" + startingPoint.toString() +
                ", endingPoint=" + endingPoint.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        // Make sure to handle case where the points are the inverse
        if (Objects.equals(startingPoint, edge.startingPoint) &&
                Objects.equals(endingPoint, edge.endingPoint)) {
            return true;
        }
        return Objects.equals(startingPoint, edge.endingPoint) &&
                Objects.equals(endingPoint, edge.startingPoint);
    }

    @Override
    public int hashCode() {
        return startingPoint.hashCode() + endingPoint.hashCode();
    }

}
