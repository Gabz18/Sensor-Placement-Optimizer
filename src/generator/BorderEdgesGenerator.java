package generator;

import java.util.*;

public class BorderEdgesGenerator {

    private List<BorderPoint> leftBorderPoints = new ArrayList<>();
    private List<BorderPoint> topBorderPoints = new ArrayList<>();
    private List<BorderPoint> rightBorderPoints = new ArrayList<>();
    private List<BorderPoint> bottomBorderPoints = new ArrayList<>();

    private boolean leftBorderHasNoPoint = false;
    private boolean topBorderHasNoPoint = false;
    private boolean rightBorderHasNoPoint = false;
    private boolean bottomBorderHasNoPoint = false;

    private List<Edge> edges;
    private double xPlaneSize;
    private double yPlaneSize;

    public BorderEdgesGenerator(List<Edge> edges, double xPlaneSize, double yPlaneSize) {
        this.edges = edges;
        this.xPlaneSize = xPlaneSize;
        this.yPlaneSize = yPlaneSize;
    }

    public List<Edge> generateBorderEdges() {
        edges.forEach(edge -> {
            this.collectBorderPoint(edge.getStartingPoint(), edge);
            this.collectBorderPoint(edge.getEndingPoint(), edge);
        });

        this.sortBorderPointsLists();
        this.checkForPointlessBorder();
        this.addMissingCornerPoints();
        return this.completeMissingBordersEdges();
    }

    private void collectBorderPoint(Point point, Edge relatedEdge) {
        if (point.getX() == 0) {
            leftBorderPoints.add(new BorderPoint(point, relatedEdge));
        } else if (point.getX() == this.xPlaneSize) {
            rightBorderPoints.add(new BorderPoint(point, relatedEdge));
        }

        if (point.getY() == 0) {
            topBorderPoints.add(new BorderPoint(point, relatedEdge));
        } else if (point.getY() == this.yPlaneSize) {
            bottomBorderPoints.add(new BorderPoint(point, relatedEdge));
        }
    }

    private void sortBorderPointsLists() {
        BorderPointSorUtils.sortTopBorderPoints(topBorderPoints);
        BorderPointSorUtils.sortLeftBorderPoints(leftBorderPoints);
        BorderPointSorUtils.sortBottomBorderPoints(bottomBorderPoints);
        BorderPointSorUtils.sortRightBorderPoints(rightBorderPoints);
    }

    private void checkForPointlessBorder() {
        if (topBorderPoints.size() == 0) topBorderHasNoPoint = true;
        if (leftBorderPoints.size() == 0) leftBorderHasNoPoint = true;
        if (bottomBorderPoints.size() == 0) bottomBorderHasNoPoint = true;
        if (rightBorderPoints.size() == 0) rightBorderHasNoPoint = true;
    }

    private void addMissingCornerPoints() {
        // We are browsing the borders clock wise
        if (topBorderHasNoPoint) {
            BorderPoint cornerPoint = new BorderPoint(
                    0d,
                    0d,
                    leftBorderPoints.get(leftBorderPoints.size() - 1).getRelatedEdge()
            );
            BorderPoint leftBorderPoint = new BorderPoint(
                    0d, 0d
            );
            // Since there is no edge touching the top border, we know there has to be a edge touching the left border
            topBorderPoints.add(0, cornerPoint);
            leftBorderPoints.add(leftBorderPoint);
        } else if (topBorderPoints.get(0).getX() != 0) {
            BorderPoint cornerPoint = new BorderPoint(0d, 0d);
            topBorderPoints.add(0, cornerPoint);
            leftBorderPoints.add(cornerPoint);
        }

        if (rightBorderHasNoPoint) {
            // Since there is no edge touching the right border, we know there has to be a edge touching the top border
            // But still the top corner point should act as if there was an edge touching this side
            BorderPoint cornerPoint = new BorderPoint(
                    xPlaneSize,
                    0d,
                    topBorderPoints.get(topBorderPoints.size() - 1).getRelatedEdge()
            );
            BorderPoint topCornerPoint = new BorderPoint(
                    xPlaneSize,
                    0d
            );
            topBorderPoints.add(topCornerPoint);
            rightBorderPoints.add(0, cornerPoint);
        } else if (rightBorderPoints.get(0).getY() != 0) {
            BorderPoint cornerPoint = new BorderPoint(xPlaneSize, 0d);
            rightBorderPoints.add(0, cornerPoint);
            topBorderPoints.add(cornerPoint);
        }

        if (bottomBorderHasNoPoint) {
            // Since there is no edge touching the bottom border, we know there has to be a edge touching the right border
            BorderPoint cornerPoint = new BorderPoint(
                    xPlaneSize,
                    yPlaneSize,
                    rightBorderPoints.get(rightBorderPoints.size() - 1).getRelatedEdge()
            );
            BorderPoint rightCornerPoint = new BorderPoint(
                    xPlaneSize, yPlaneSize
            );
            rightBorderPoints.add(rightCornerPoint);
            bottomBorderPoints.add(0, cornerPoint);
        } else if (bottomBorderPoints.get(0).getX() != xPlaneSize) {
            BorderPoint cornerPoint = new BorderPoint(xPlaneSize, yPlaneSize);
            bottomBorderPoints.add(0, cornerPoint);
            rightBorderPoints.add(cornerPoint);
        }

        if (leftBorderHasNoPoint) {
            // Since there is no edge touching the left border, we know there has to be a edge touching the bottom border
            BorderPoint cornerPoint = new BorderPoint(
                    0d,
                    yPlaneSize,
                    bottomBorderPoints.get(bottomBorderPoints.size() - 1).getRelatedEdge()
            );
            BorderPoint bottomCornerPoint = new BorderPoint(0d, yPlaneSize);
            bottomBorderPoints.add(bottomCornerPoint);
            leftBorderPoints.add(0, cornerPoint);
        } else if (leftBorderPoints.get(0).getY() != yPlaneSize) {
            BorderPoint cornerPoint = new BorderPoint(0, yPlaneSize);
            leftBorderPoints.add(0, cornerPoint);
            bottomBorderPoints.add(cornerPoint);
        }
    }

    private List<Edge> completeMissingBordersEdges() {
        List<Edge> addedEdges = new ArrayList<>();
        addedEdges.addAll(this.addBorderEdges(topBorderPoints, Border.TOP));
        addedEdges.addAll(this.addBorderEdges(rightBorderPoints, Border.RIGHT));
        addedEdges.addAll(this.addBorderEdges(bottomBorderPoints, Border.BOTTOM));
        addedEdges.addAll(this.addBorderEdges(leftBorderPoints, Border.LEFT));
        return addedEdges;
    }

    private List<Edge> addBorderEdges(List<BorderPoint> borderPoints, Border targetedBorder) {
        List<Edge> addedEdges = new ArrayList<>();
        Point edgeRightSite;
        for (int i = 1; i < borderPoints.size(); i++) {
            RelatedEdge relatedEdge;
            if (!borderPoints.get(i).equals(borderPoints.get(i - 1))) {
                // In case the point has been created in the #addMissingCornerPoints method and has no relatedEdge for now
                if (borderPoints.get(i).getRelatedEdge() != null) {
                    relatedEdge = new RelatedEdge(true, borderPoints.get(i).getRelatedEdge());
                } else {
                    relatedEdge = new RelatedEdge(false, borderPoints.get(i - 1).getRelatedEdge());
                }
                // Find correct side depending on the edge direction
                if (targetedBorder == Border.LEFT) {
                    if (leftBorderHasNoPoint) {
                        // Check the direction
                        // In this case we know the related edge is from index 0 and has to be touching the bottom border (we browsing borders clock wise)
                        if (relatedEdge.edge.getStartingPoint().getY() == this.yPlaneSize) {
                            edgeRightSite = relatedEdge.edge.getRightSite();
                        } else {
                            edgeRightSite = relatedEdge.edge.getLeftSite();
                        }
                    } else if (relatedEdge.edge.getEndingPoint().getX() == 0d) {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getRightSite() : relatedEdge.edge.getLeftSite();
                    } else {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getLeftSite() : relatedEdge.edge.getRightSite();
                    }
                } else if (targetedBorder == Border.TOP) {
                    if (topBorderHasNoPoint) {
                        // Check the direction                }
                        // In this case we know the related edge is from index 0 and has to be touching the left border (we browsing borders clock wise)
                        if (relatedEdge.edge.getStartingPoint().getX() == 0) {
                            edgeRightSite = relatedEdge.edge.getRightSite();
                        } else {
                            edgeRightSite = relatedEdge.edge.getLeftSite();
                        }
                    } else if (relatedEdge.edge.getEndingPoint().getY() == 0d) {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getRightSite() : relatedEdge.edge.getLeftSite();
                    } else {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getLeftSite() : relatedEdge.edge.getRightSite();
                    }
                } else if (targetedBorder == Border.RIGHT) {
                    if (rightBorderHasNoPoint) {
                        // In this case we know the related edge is from index 0 and has to be touching the top border (we browsing borders clock wise)
                        // Check the direction
                        if (relatedEdge.edge.getStartingPoint().getY() == 0) {
                            edgeRightSite = relatedEdge.edge.getRightSite();
                        } else {
                            edgeRightSite = relatedEdge.edge.getLeftSite();
                        }
                    } else if (relatedEdge.edge.getEndingPoint().getX() == xPlaneSize) {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getRightSite() : relatedEdge.edge.getLeftSite();
                    } else {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getLeftSite() : relatedEdge.edge.getRightSite();
                    }
                    // Border = Bottom
                } else {
                    if (bottomBorderHasNoPoint) {
                        // In this case we know the related edge is from index 0 and has to be touching the right border (we browsing borders clock wise)
                        // Check the direction
                        if (relatedEdge.edge.getStartingPoint().getX() == xPlaneSize) {
                            edgeRightSite = relatedEdge.edge.getRightSite();
                        } else {
                            edgeRightSite = relatedEdge.edge.getLeftSite();
                        }
                    } else if (relatedEdge.edge.getEndingPoint().getY() == yPlaneSize) {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getRightSite() : relatedEdge.edge.getLeftSite();
                    } else {
                        edgeRightSite = relatedEdge.fromCurrentIndex ? relatedEdge.edge.getLeftSite() : relatedEdge.edge.getRightSite();
                    }
                }
                addedEdges.add(new Edge(
                        borderPoints.get(i - 1),
                        borderPoints.get(i),
                        null,
                        edgeRightSite
                ));
            }
        }
        return addedEdges;
    }

    private enum Border {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

    private static class RelatedEdge {

        private final boolean fromCurrentIndex;
        private final Edge edge;

        public RelatedEdge(boolean fromCurrentIndex, Edge edge) {
            this.fromCurrentIndex = fromCurrentIndex;
            this.edge = edge;
        }
    }
}
