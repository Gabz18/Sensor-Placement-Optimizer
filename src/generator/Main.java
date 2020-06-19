package generator;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    private static Voronoi voronoi;
    private static boolean displayDiagram = true;
    private static double sitesRadius = 0d;

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<Edge> voronoiDiagramEdges = voronoi.generateDiagram();
        BorderEdgesGenerator gen = new BorderEdgesGenerator(voronoiDiagramEdges, voronoi.getxPlaneSize(), voronoi.getyPlaneSize());
        List<Edge> borderEdges = gen.generateBorderEdges();
        voronoiDiagramEdges.addAll(borderEdges);

        Map<Point, Set<Edge>> voronoiCells = VoronoiCellExtractor.listVoronoiCells(voronoiDiagramEdges);
        Map<Point, List<Point>> cellSiteEdgesMap = VoronoiCellBeautifier.buildVonoroiCellsLinkedPointsLists(voronoiCells);
        for (Point site : cellSiteEdgesMap.keySet()) {
            StringBuilder sb = new StringBuilder(site.toString() + "|");
            // Dont display the first point again
            sb.append("(");
            for (int i = 0; i < cellSiteEdgesMap.get(site).size() - 1; i++) {
                sb.append(cellSiteEdgesMap.get(site).get(i).toString());
                if (i < cellSiteEdgesMap.get(site).size() - 2) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            System.out.println(sb.toString());
        }

        if (displayDiagram) {
            Collection<Node> sites = voronoi.getSites().stream().map(Point::getRepresentation).collect(Collectors.toList());
            Collection<Node> edges = voronoiDiagramEdges.stream().map(Edge::getRepresentation).collect(Collectors.toList());

            Collection<Node> drawings = new ArrayList<Node>() {{
                addAll(sites);
                addAll(edges);
            }};
            if (sitesRadius != 0d) {
                drawings.addAll(voronoi.getSites()
                        .stream()
                        .map(point -> point.getCoverageRepresentation(sitesRadius))
                        .collect(Collectors.toList()));
            }
            showDiagram(primaryStage, drawings);
        } else {
            System.exit(1);
        }

    }

    private static void showDiagram(Stage primaryStage, Collection<Node> drawings) {
        Group root = new Group(drawings);
        primaryStage.setTitle("Voronoi Generator");
        primaryStage.setScene(new Scene(root, voronoi.getxPlaneSize(), voronoi.getyPlaneSize()));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        handleArgs(args);
        launch(args);
    }

    private static void handleArgs(String[] args) {
        double height = 0d;
        double width = 0d;
        List<Point> sites = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (i == args.length - 1) {
                System.err.println("Missing value for argument : " + args[i]);
                System.exit(-1);
            }
            String s = args[i];
            switch (s) {
                case "--no-display": {
                    displayDiagram = false;
                    break;
                }
                case "-r": {
                    try {
                        sitesRadius = Double.parseDouble(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid value for argument : " + args[i]);
                        System.exit(-1);
                    }
                    break;
                }
                case "-h":
                    try {
                        height = Double.parseDouble(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid value for argument : " + args[i]);
                        System.exit(-1);
                    }
                    break;
                case "-w":
                    try {
                        width = Double.parseDouble(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid value for argument : " + args[i]);
                        System.exit(-1);
                    }
                    break;
                case "-s":
                    try {
                        String[] splitString = args[i + 1].split(":");
                        sites.add(new Point(Double.parseDouble(splitString[0]), Double.parseDouble(splitString[1])));
                        i++;
                    } catch (Exception e) {
                        System.err.println("Invalid value for argument : " + args[i]);
                        System.exit(-1);
                    }
                    break;
                default:
                    System.err.println("Unrecognized argument : " + args[i]);
                    System.exit(-1);
            }
        }
        if (width == 0d || height == 0d) {
            System.err.println("Missing width (-w) or height (-h) argument");
            System.exit(-1);
        }
        if (sites.size() < 2) {
            System.err.println("At least 2 sites should be provided (-s 1.2:3.4)");
            System.exit(-1);
        }
        voronoi = new Voronoi(sites, width, height);
    }
}
