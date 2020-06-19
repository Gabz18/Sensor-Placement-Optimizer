package generator;

import java.util.*;

public class VoronoiCellBeautifier {

    public static Map<Point, List<Point>> buildVonoroiCellsLinkedPointsLists(Map<Point, Set<Edge>> cellsEdges) {
        Map<Point, List<Point>> result = new HashMap<>();
        for (Point point : cellsEdges.keySet()) {
            result.put(point, buildVoronoiCellLinkedPointsList(cellsEdges.get(point)));
        }
        return result;
    }

    private static List<Point> buildVoronoiCellLinkedPointsList(Set<Edge> voronoiCellEdges) {
        List<Edge> edges = new ArrayList<>(voronoiCellEdges);
        List<Point> linkedPointsList = new ArrayList<>();
        Edge edge = edges.remove(0);
        linkedPointsList.add(edge.getStartingPoint());
        linkedPointsList.add(edge.getEndingPoint());
        while (edges.size() > 0) {
            for (Edge e : edges) {
                if (linkedPointsList.get(0).equals(e.getStartingPoint())) {
                    linkedPointsList.add(0, e.getEndingPoint());
                    edges.remove(e);
                    break;
                } else if (linkedPointsList.get(0).equals(e.getEndingPoint())) {
                    linkedPointsList.add(0, e.getStartingPoint());
                    edges.remove(e);
                    break;
                }
            }
        }
        return linkedPointsList;
    }

}
