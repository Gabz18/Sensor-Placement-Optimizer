package generator;


// represents the beach line
// can either be a site that is the center of a parabola
// or can be a vertex that bisects two sites
public class Parabola {

    public static int IS_FOCUS = 0;
    public static int IS_VERTEX = 1;

    private int type;
    private Point point; // if is focus
    private Edge edge; // if is vertex
    private Event event; // a parabola with a focus can disappear in a circle event

    private Parabola parent;
    private Parabola leftChild;
    private Parabola rightChild;

    public Parabola () {
        type = IS_VERTEX;
    }

    public Parabola (Point p) {
        point = p;
        type = IS_FOCUS;
    }

    public int getType() {
        return type;
    }

    public Point getPoint() {
        return point;
    }

    public Edge getEdge() {
        return edge;
    }

    public Event getEvent() {
        return event;
    }

    public Parabola getLeftChild() {
        return leftChild;
    }

    public Parabola getRightChild() {
        return rightChild;
    }

    public Parabola getParent() {
        return parent;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public void setLeftChild (Parabola p) {
        leftChild = p;
        p.parent = this;
    }

    public void setRightChild (Parabola p) {
        rightChild = p;
        p.parent = this;
    }

    public void setParent(Parabola parent) {
        this.parent = parent;
    }

    public String toString() {
        if (type == IS_FOCUS) {
            return "Focus at " + point;
        }
        else{
            return "Vertex/Edge beginning at " + edge.getStartingPoint().toString();
        }
    }

    // returns the closest left site (focus of parabola)
    public static Parabola getLeft(Parabola p) {
        return getLeftChild(getLeftParent(p));
    }

    // returns closest right site (focus of parabola)
    public static Parabola getRight(Parabola p) {
        return getRightChild(getRightParent(p));
    }

    // returns the closest parent on the left
    public static Parabola getLeftParent(Parabola p) {
        Parabola parent = p.parent;
        if (parent == null) return null;
        Parabola last = p;
        while (parent.leftChild == last) {
            if(parent.parent == null) return null;
            last = parent;
            parent = parent.parent;
        }
        return parent;
    }

    // returns the closest parent on the right
    public static Parabola getRightParent(Parabola p) {
        Parabola parent = p.parent;
        if (parent == null) return null;
        Parabola last = p;
        while (parent.rightChild == last) {
            if(parent.parent == null) return null;
            last = parent;
            parent = parent.parent;
        }
        return parent;
    }

    // returns closest site (focus of another parabola) to the left
    public static Parabola getLeftChild(Parabola p) {
        if (p == null) return null;
        Parabola child = p.leftChild;
        while(child.type == IS_VERTEX) child = child.rightChild;
        return child;
    }

    // returns closest site (focus of another parabola) to the right
    public static Parabola getRightChild(Parabola p) {
        if (p == null) return null;
        Parabola child = p.rightChild;
        while(child.type == IS_VERTEX) child = child.leftChild;
        return child;
    }

}
