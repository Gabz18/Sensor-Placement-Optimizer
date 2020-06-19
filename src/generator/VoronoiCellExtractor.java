package generator;

import java.util.*;

public class VoronoiCellExtractor {

    public static Map<Point, Set<Edge>> listVoronoiCells(List<Edge> voronoiDiagramEdges) {
        Map<Point, Set<Edge>> siteEdgesMap = new HashMap<>();

        for (Edge edge: voronoiDiagramEdges) {
            if (edge.getLeftSite() != null) {
                siteEdgesMap.computeIfAbsent(edge.getLeftSite(), k -> new HashSet<>());
                siteEdgesMap.get(edge.getLeftSite()).add(edge);
            }
            if (edge.getRightSite() != null) {
                siteEdgesMap.computeIfAbsent(edge.getRightSite(), k -> new HashSet<>());
                siteEdgesMap.get(edge.getRightSite()).add(edge);
            }
        }

        return siteEdgesMap;
    }
}
