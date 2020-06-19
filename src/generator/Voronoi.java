package generator;

import java.util.*;

public class Voronoi {

    private final List<Point> sites;
    private final double xPlaneSize;
    private final double yPlaneSize;
    private Parabola initialParabola;
    private Queue<Event> eventQueue;
    private List<Edge> edges;
    private double currentY;

    public Voronoi(List<Point> sites, double xPlaneSize, double yPlaneSize) {
        this.sites = sites;
        this.ensureSitesGotDifferentYCoords();
        this.xPlaneSize = xPlaneSize;
        this.yPlaneSize = yPlaneSize;
        this.initialParabola = null;
    }

    public List<Point> getSites() {
        return sites;
    }

    public double getxPlaneSize() {
        return xPlaneSize;
    }

    public double getyPlaneSize() {
        return yPlaneSize;
    }

    private void ensureSitesGotDifferentYCoords() {
        List<Double> metValues = new ArrayList<>();
        for (Point site : sites) {
            while (metValues.indexOf(site.getY()) > -1) {
                site.setY(site.getY() + 0.00000000001d);
            }
            metValues.add(site.getY());
        }
    }

    public List<Edge> generateDiagram() {
        this.edges = new ArrayList<>();
        this.eventQueue = new PriorityQueue<>();
        for (Point site : sites) {
            eventQueue.add(new Event(site, EventType.POINT));
        }

        while (!eventQueue.isEmpty()) {
            Event currEvent = eventQueue.remove();
            this.currentY = currEvent.getPoint().getY();

            if (currEvent.getType() == EventType.POINT) {
                processPointEvent(currEvent);
            } else if (currEvent.getType() == EventType.VERTEX) {
                processVertexEvent(currEvent);
            } else {
                System.exit(-1);
            }
        }

        this.currentY = yPlaneSize + xPlaneSize;

        endEdges(this.initialParabola); // close off any dangling edges

        // get rid of those crazy infinite lines
        for (Edge e : edges) {
            if (e.getNeighbor() != null) {
                e.setStartingPoint(e.getNeighbor().getEndingPoint());
                e.setNeighbor(null);
            }
        }
        this.removeDuplicateEdges();
        this.correctOutOfPlaneEdgeVertices();
        return edges;
    }

    private void endEdges(Parabola p) {
        if (p.getType() == Parabola.IS_FOCUS) {
            p = null;
            return;
        }

        double x = getXofEdge(p);
        p.getEdge().setEndingPoint(new Point(x, p.getEdge().getSlope() * x + p.getEdge().getYint()));
        edges.add(p.getEdge());

        endEdges(p.getLeftChild());
        endEdges(p.getRightChild());

        p = null;
    }

    private void correctOutOfPlaneEdgeVertices() {
        // Edges to be removed because both points are out of the plane.
        List<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getStartingPoint().getX() >= this.xPlaneSize && edge.getEndingPoint().getX() >= this.xPlaneSize) {
                edgesToRemove.add(edge);
            } else if (edge.getStartingPoint().getX() <= 0d && edge.getEndingPoint().getX() <= 0d) {
                edgesToRemove.add(edge);
            } else if (edge.getStartingPoint().getY() >= this.yPlaneSize && edge.getEndingPoint().getY() >= this.yPlaneSize) {
                edgesToRemove.add(edge);
            } else if (edge.getStartingPoint().getY() <= 0d && edge.getEndingPoint().getY() <= 0d) {
                edgesToRemove.add(edge);
            } else {

                // Always Instantiate another point object in case two edges are sharing the same object reference.
                if (edge.getStartingPoint().getX() > this.getxPlaneSize()) {
                    Point point = new Point(
                            this.getxPlaneSize(),
                            this.computeEdgePointYCoordinate(this.getxPlaneSize(), edge.getStartingPoint(), edge.getSlope())
                    );
                    edge.setStartingPoint(point);
                }
                if (edge.getStartingPoint().getX() < 0) {
                    Point point = new Point(
                            0d,
                            this.computeEdgePointYCoordinate(0d, edge.getStartingPoint(), edge.getSlope())
                    );
                    edge.setStartingPoint(point);
                }
                if (edge.getStartingPoint().getY() > this.getyPlaneSize()) {
                    Point point = new Point(
                            this.computeEdgePointXCoordinate(this.yPlaneSize, edge.getStartingPoint(), edge.getSlope()),
                            this.yPlaneSize
                    );
                    edge.setStartingPoint(point);
                }
                if (edge.getStartingPoint().getY() < 0) {
                    Point point = new Point(
                            this.computeEdgePointXCoordinate(0d, edge.getStartingPoint(), edge.getSlope()),
                            0d
                    );
                    edge.setStartingPoint(point);
                }
                if (edge.getEndingPoint().getX() > this.getxPlaneSize()) {
                    Point point = new Point(
                            this.getxPlaneSize(),
                            this.computeEdgePointYCoordinate(this.getxPlaneSize(), edge.getEndingPoint(), edge.getSlope())
                    );
                    edge.setEndingPoint(point);
                }
                if (edge.getEndingPoint().getX() < 0) {
                    Point point = new Point(
                            0d,
                            this.computeEdgePointYCoordinate(0d, edge.getEndingPoint(), edge.getSlope())
                    );
                    edge.setEndingPoint(point);
                }
                if (edge.getEndingPoint().getY() > this.getyPlaneSize()) {
                    Point point = new Point(
                            this.computeEdgePointXCoordinate(this.yPlaneSize, edge.getEndingPoint(), edge.getSlope()),
                            this.yPlaneSize
                    );
                    edge.setEndingPoint(point);
                }
                if (edge.getEndingPoint().getY() < 0) {
                    Point point = new Point(
                            this.computeEdgePointXCoordinate(0d, edge.getEndingPoint(), edge.getSlope()),
                            0d
                    );
                    edge.setEndingPoint(point);
                }
            }
        }
        edgesToRemove.forEach(e -> this.edges.remove(e));
    }

    private double computeEdgePointYCoordinate(double xCoordinate, Point sampleEdgePoint, double slope) {
        double b = sampleEdgePoint.getY() - sampleEdgePoint.getX() * slope;
        return xCoordinate * slope + b;
    }

    private double computeEdgePointXCoordinate(double yCoordinate, Point sampleEdgePoint, double slope) {
        double b = sampleEdgePoint.getY() - sampleEdgePoint.getX() * slope;
        return (yCoordinate - b) / slope;
    }

    private void removeDuplicateEdges() {
        Set<Edge> e = new HashSet<>(this.edges);
        this.edges.clear();
        this.edges.addAll(e);
    }

    private void processPointEvent(Event pointEvent) {
        if (initialParabola == null) {
            this.initialParabola = new Parabola(pointEvent.getPoint());
            return;
        }
        // find parabola on beach line right above p
        Parabola par = getParabolaByX(pointEvent.getPoint().getX());
        if (par.getEvent() != null) {
            this.eventQueue.remove(par.getEvent());
            par.setEvent(null);
        }

        // create new dangling edge; bisects parabola focus and p
        Point start = new Point(pointEvent.getPoint().getX(), getY(par.getPoint(), pointEvent.getPoint().getX()));
        Edge el = new Edge(start, par.getPoint(), pointEvent.getPoint());
        Edge er = new Edge(start, pointEvent.getPoint(), par.getPoint());
        el.setNeighbor(er);
        er.setNeighbor(el);
        par.setEdge(el);
        par.setType(Parabola.IS_VERTEX);

        // replace original parabola par with p0, p1, p2
        Parabola p0 = new Parabola(par.getPoint());
        Parabola p1 = new Parabola(pointEvent.getPoint());
        Parabola p2 = new Parabola(par.getPoint());

        par.setLeftChild(p0);
        par.setRightChild(new Parabola());
        par.getRightChild().setEdge(er);
        par.getRightChild().setLeftChild(p1);
        par.getRightChild().setRightChild(p2);

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void processVertexEvent(Event vertexEvent) {
// find p0, p1, p2 that generated this event from left to right
        Parabola p1 = vertexEvent.getArc();
        Parabola xl = Parabola.getLeftParent(p1);
        Parabola xr = Parabola.getRightParent(p1);
        Parabola p0 = Parabola.getLeftChild(xl);
        Parabola p2 = Parabola.getRightChild(xr);

        // remove associated events since the points will be altered
        if (p0.getEvent() != null) {
            this.eventQueue.remove(p0.getEvent());
            p0.setEvent(null);
        }
        if (p2.getEvent() != null) {
            this.eventQueue.remove(p2.getEvent());
            p2.setEvent(null);
        }

        Point p = new Point(vertexEvent.getPoint().getX(), getY(p1.getPoint(), vertexEvent.getPoint().getX())); // new vertex

        // end edges!
        xl.getEdge().setEndingPoint(p);
        xr.getEdge().setEndingPoint(p);
        edges.add(xl.getEdge());
        edges.add(xr.getEdge());

        // start new bisector (edge) from this vertex on which ever original edge is higher in tree
        Parabola higher = new Parabola();
        Parabola par = p1;
        while (par != this.initialParabola) {
            par = par.getParent();
            if (par == xl) higher = xl;
            if (par == xr) higher = xr;
        }
        higher.setEdge(new Edge(p, p0.getPoint(), p2.getPoint()));

        // delete p1 and parent (boundary edge) from beach line
        Parabola gparent = p1.getParent().getParent();
        if (p1.getParent().getLeftChild() == p1) {
            if (gparent.getLeftChild() == p1.getParent()) gparent.setLeftChild(p1.getParent().getRightChild());
            if (gparent.getRightChild() == p1.getParent()) gparent.setRightChild(p1.getParent().getRightChild());
        } else {
            if (gparent.getLeftChild() == p1.getParent()) gparent.setLeftChild(p1.getParent().getLeftChild());
            if (gparent.getRightChild() == p1.getParent()) gparent.setRightChild(p1.getParent().getLeftChild());
        }

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    // adds circle event if foci a, b, c lie on the same circle
    private void checkCircleEvent(Parabola b) {

        Parabola lp = Parabola.getLeftParent(b);
        Parabola rp = Parabola.getRightParent(b);

        if (lp == null || rp == null) return;

        Parabola a = Parabola.getLeftChild(lp);
        Parabola c = Parabola.getRightChild(rp);

        if (a == null || c == null || a.getPoint() == c.getPoint()) return;

        if (ccw(a.getPoint(), b.getPoint(), c.getPoint()) != 1) return;

        // edges will intersect to form a vertex for a circle event
        Point start = getEdgeIntersection(lp.getEdge(), rp.getEdge());
        if (start == null) return;

        // compute radius
        double dx = b.getPoint().getX() - start.getX();
        double dy = b.getPoint().getY() - start.getY();
        double d = Math.sqrt((dx * dx) + (dy * dy));
        if (start.getY() + d < this.currentY) return; // must be after sweep line

        Point ep = new Point(start.getX(), start.getY() + d);
        //System.out.println("added circle event "+ ep);

        // add circle event
        Event e = new Event(ep, EventType.VERTEX);
        e.setArc(b);
        b.setEvent(e);
        this.eventQueue.add(e);
    }

    public int ccw(Point a, Point b, Point c) {
        double area2 = (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1;
        else return 0;
    }

    // returns intersection of the lines of with vectors a and b
    private Point getEdgeIntersection(Edge a, Edge b) {

        if (b.getSlope() == a.getSlope() && b.getYint() != a.getYint()) return null;

        double x = (b.getYint() - a.getYint()) / (a.getSlope() - b.getSlope());
        double y = a.getSlope() * x + a.getYint();

        return new Point(x, y);
    }

    // returns current x-coordinate of an unfinished edge
    private double getXofEdge(Parabola par) {
        //find intersection of two parabolas
        Parabola left = Parabola.getLeftChild(par);
        Parabola right = Parabola.getRightChild(par);

        Point p = left.getPoint();
        Point r = right.getPoint();

        // Hotfix
        /*
        if (p.getY() == r.getY()) {
            if (r.getX() > p.getX()) {
                return (r.getX() - p.getX());
            } else {
                return (p.getX() - r.getX());
            }
        }
         */
        double dp = 2 * (p.getY() - this.currentY);
        double a1 = 1 / dp;
        double b1 = -2 * p.getX() / dp;
        double c1 = (p.getX() * p.getX() + p.getY() * p.getY() - this.currentY * this.currentY) / dp;

        double dp2 = 2 * (r.getY() - this.currentY);
        double a2 = 1 / dp2;
        double b2 = -2 * r.getX() / dp2;
        double c2 = (r.getX() * r.getX() + r.getY() * r.getY() - this.currentY * this.currentY) / dp2;

        double a = a1 - a2;
        double b = b1 - b2;
        double c = c1 - c2;

        double disc = b * b - 4 * a * c;
        double x1 = (-b + Math.sqrt(disc)) / (2 * a);
        double x2 = (-b - Math.sqrt(disc)) / (2 * a);

        double ry;
        if (p.getY() > r.getY()) ry = Math.max(x1, x2);
        else ry = Math.min(x1, x2);

        return ry;
    }

    // returns parabola above this x coordinate in the beach line
    private Parabola getParabolaByX(double xx) {
        Parabola par = this.initialParabola;
        double x = 0;
        while (par.getType() == Parabola.IS_VERTEX) {
            x = getXofEdge(par);
            if (x > xx) par = par.getLeftChild();
            else par = par.getRightChild();
        }
        return par;
    }

    // find corresponding y-coordinate to x on parabola with focus p
    private double getY(Point p, double x) {
        // determine equation for parabola around focus p
        double dp = 2 * (p.getY() - this.currentY);
        // Hot fix tres artisanal

        /*
        if (dp == 0d) {
            dp = 0.0000000000001d;
        }

         */
        double a1 = 1 / dp;
        double b1 = -2 * p.getX() / dp;
        double c1 = (p.getX() * p.getX() + p.getY() * p.getY() - this.currentY * this.currentY) / dp;
        double result = (a1 * x * x + b1 * x + c1);
        return Math.min(result, this.yPlaneSize);
    }
}
