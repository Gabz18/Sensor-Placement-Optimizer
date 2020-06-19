package generator;

public class Event implements Comparable<Event> {

    private Point point;
    private Parabola arc;
    private final EventType type;

    public Event(Point point, EventType type) {
        this.point = point;
        this.type = type;
        this.arc = null;
    }

    public Point getPoint() {
        return point;
    }

    public Parabola getArc() {
        return arc;
    }

    public EventType getType() {
        return type;
    }

    public void setArc(Parabola arc) {
        this.arc = arc;
    }

    @Override
    public int compareTo(Event o) {
        return this.getPoint().compareTo(o.getPoint());
    }
}
