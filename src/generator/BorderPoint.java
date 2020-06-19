package generator;

public class BorderPoint extends Point {

    // The edge cutting the border
    private final Edge relatedEdge;

    public BorderPoint(double x, double y) {
        super(x, y);
        relatedEdge = null;
    }

    public BorderPoint(double x, double y, Edge relatedEdge) {
        super(x, y);
        this.relatedEdge = relatedEdge;
    }

    public BorderPoint(Point point, Edge relatedEdge) {
        super(point.getX(), point.getY());
        this.relatedEdge = relatedEdge;
    }

    public Edge getRelatedEdge() {
        return relatedEdge;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(Point o) {
        return super.compareTo(o);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
