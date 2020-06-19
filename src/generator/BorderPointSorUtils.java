package generator;

import java.util.List;

public class BorderPointSorUtils {
    /*
        This class sorts points in a manner that if two points are equals, they will be sorted based on their related edge
        Since points without related edge are generated only if they do not already exist, we do not have to worry about
        null edges because we will never access them in this case.
     */


    public static void sortTopBorderPoints(List<BorderPoint> points) {
        points.sort((a, b) -> {
            if (a.getX() > b.getX()) {
                return 1;
            } else if (a.getX() < b.getX()) {
                return -1;
            } else {
                if (getOtherEdgePoint(a).getX() > getOtherEdgePoint(b).getX()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    public static void sortRightBorderPoints(List<BorderPoint> points) {
        points.sort((a, b) -> {
            if (a.getY() > b.getY()) {
                return 1;
            } else if (a.getY() < b.getY()) {
                return -1;
            } else {
                if (getOtherEdgePoint(a).getY() > getOtherEdgePoint(b).getY()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    // Inverse the sorting for the two following methods because borders are being browsed clock wise

    public static void sortBottomBorderPoints(List<BorderPoint> points) {
        points.sort((a, b) -> {
            if (a.getX() > b.getX()) {
                return -1;
            } else if (a.getX() < b.getX()) {
                return 1;
            } else {
                if (getOtherEdgePoint(a).getX() > getOtherEdgePoint(b).getX()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    public static void sortLeftBorderPoints(List<BorderPoint> points) {
        points.sort((a, b) -> {
            if (a.getY() > b.getY()) {
                return -1;
            } else if (a.getY() < b.getY()) {
                return 1;
            } else {
                if (getOtherEdgePoint(a).getY() > getOtherEdgePoint(b).getY()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    public static Point getOtherEdgePoint(BorderPoint point) {
        if (point.getRelatedEdge().getEndingPoint().equals(point)) {
            return point.getRelatedEdge().getStartingPoint();
        } else {
            return point.getRelatedEdge().getEndingPoint();
        }
    }
}
