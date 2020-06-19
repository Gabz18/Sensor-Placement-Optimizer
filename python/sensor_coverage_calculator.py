import copy
import itertools
import math

from model import Site, Edge, segment_intersects


def compute_solution_plane_coverage_percentage(sites: [Site], x_plane_size, y_plane_size):
    coverage_area = 0
    for site in sites:
        coverage_area += compute_site_coverage(site)
    return coverage_area / (x_plane_size * y_plane_size) * 100


def compute_site_coverage(site: Site):
    cell_area = compute_polygon_area(site.cell_corners)
    circle_intersection_edges = []
    for edge in build_cell_edges(site):
        # Take the edge as an infinite line
        edge_line_circle_intersection_points = circle_line_segment_intersection(
            (edge.x_start, edge.y_start), (edge.x_end, edge.y_end), site
        )
        # List all edges that intersect the circle
        if edge_line_circle_intersection_points is not None:
            circle_intersection_edges.append(
                Edge(
                    edge_line_circle_intersection_points[0][0],
                    edge_line_circle_intersection_points[0][1],
                    edge_line_circle_intersection_points[1][0],
                    edge_line_circle_intersection_points[1][1]
                )
            )

    # Generate every possible edge pairs
    for pair in itertools.combinations(circle_intersection_edges, 2):
        segment_a = pair[0]
        segment_b = pair[1]
        segments_intersection_point_exists = segment_intersects(
            (segment_a.x_start, segment_a.y_start), (segment_a.x_end, segment_a.y_end),
            (segment_b.x_start, segment_b.y_start), (segment_b.x_end, segment_b.y_end)
        )
        if segments_intersection_point_exists:
            segments_intersection_point = intersection(
                line(segment_a.x_start, segment_a.y_start, segment_a.x_end, segment_a.y_end),
                line(segment_b.x_start, segment_b.y_start, segment_b.x_end, segment_b.y_end)
            )
            # Check if they intersect in the circle
            if compute_point_distance(
                    site.x,
                    site.y,
                    segments_intersection_point[0],
                    segments_intersection_point[1]
            ) < site.coverage_radius:
                # Update current edge point to put it on the cell edge (other edge will be updated on its iteration)
                segment_a.define_edge_point_to_keep(site, segment_b.origin_state, segments_intersection_point)
                segment_b.define_edge_point_to_keep(site, segment_a.origin_state, segments_intersection_point)

    # This list will hold every group edges that are connected to each other
    solo_edges = []
    polygon_edges_groups = []
    for group in list_connected_edges_group(circle_intersection_edges):
        if len(group) == 1:
            solo_edges.append(group[0])
        else:
            polygon_edges_groups.append(group)

    groups_coverage_area = 0
    unconnected_group_edges_end_pairs = []
    for group in polygon_edges_groups:
        groups_coverage_area += compute_polygon_area(list_polygon_corners(group))
        unconnected_edges = find_unconnected_edges_ends(group)
        if len(unconnected_edges) > 0:
            unconnected_group_edges_end_pairs.append(unconnected_edges)

    if len(polygon_edges_groups) > 0:
        # Now every unconnected group unconnected points pair will be chained by searching for the closer one
        # This list holds tuples of points (which are also represented as tuples)

        line_segment_area = 0
        central_coverage_area = 0

        if len(unconnected_group_edges_end_pairs) > 0:
            linked_pair_list = [unconnected_group_edges_end_pairs[0]]
            pairs_copy = copy.copy(unconnected_group_edges_end_pairs[1:])
            while len(pairs_copy) > 0:
                closer_pair = find_closer_point_pair_then_sort_pair(
                    linked_pair_list[len(linked_pair_list) - 1][1],
                    pairs_copy
                )
                linked_pair_list.append(closer_pair)
                pairs_copy.remove(closer_pair)

            # Unpack points into a list to compute central polygon area and also build the pair of points that will be used
            # to compute segment circle area remaining
            corners = []
            segments_circle_points = []
            for pair in linked_pair_list:
                corners.append(pair[0])
                corners.append(pair[1])
                if len(segments_circle_points) == 0:
                    segments_circle_points.append([pair[1]])
                else:
                    last_segment_circle_point_item = segments_circle_points[len(segments_circle_points) - 1]
                    if len(last_segment_circle_point_item) == 1:
                        last_segment_circle_point_item.append(pair[0])
                    else:
                        segments_circle_points.append([pair[1]])

            segments_circle_points[len(segments_circle_points) - 1].append(linked_pair_list[0][0])
            # Build central polygon if there is at least 2 connected edges groups -> meaning 4 corners
            if len(corners) > 3:
                central_coverage_area = compute_polygon_area(corners)
            for chord in segments_circle_points:
                line_segment_area += compute_circle_segment_area(site.coverage_radius, chord[0], chord[1], site)
        coverage_area = groups_coverage_area + central_coverage_area + line_segment_area
    else:
        # Handle case where no intersecting edges is met and the coverage is the full circle till now
        coverage_area = compute_circle_area(site.coverage_radius)

    # Remove every unconnected segment circle area from coverage area
    for edge in solo_edges:
        coverage_area -= compute_circle_segment_area(
            site.coverage_radius, (edge.x_start, edge.y_start), (edge.x_end, edge.y_end), site
        )

    # Add tolerance to small overflow that could be due to floating point number precision not being perfectly accurate
    if float("%.2f" % coverage_area) > float("%.2f" % cell_area):
        raise Exception("Error in computing site coverage area")
    return coverage_area


def find_closer_point_pair_then_sort_pair(point: [float], pairs: [[float]]):
    # We know that the same point cant be met twice
    closer_point_pair = None
    closer_point_pair_dist = 0
    for pair in pairs:
        if not point == pair:
            p1 = pair[0][0], pair[0][1]
            p2 = pair[1][0], pair[1][1]
            p1_dist = compute_point_distance(point[0], point[1], p1[0], p1[1])
            p2_dist = compute_point_distance(point[0], point[1], p2[0], p2[1])
            if closer_point_pair is None:
                closer_point_pair = (p1, p2)
                closer_point_pair_dist = p1_dist
            if p1_dist < closer_point_pair_dist:
                closer_point_pair = (p1, p2)
                closer_point_pair_dist = p1_dist
            if p2_dist < closer_point_pair_dist:
                closer_point_pair = (p2, p1)
                closer_point_pair_dist = p2_dist
    return closer_point_pair


def find_unconnected_edges_ends(edges: [Edge]) -> [[float]]:
    corners = []
    for edge in edges:
        if not edge.updated_end:
            corners.append((edge.x_end, edge.y_end))
        if not edge.updated_start:
            corners.append((edge.x_start, edge.y_start))
    if len(corners) > 2:
        raise Exception("Error in polygon building process")
    return corners


def list_polygon_corners(edges: [Edge]) -> [[float]]:
    corners = []
    for edge in edges:
        point = (edge.x_start, edge.y_start)
        if point not in corners:
            corners.append(point)
        point = (edge.x_end, edge.y_end)
        if point not in corners:
            corners.append(point)
    return corners


def line(p1_x, p1_y, p2_x, p2_y):
    A = (p1_y - p2_y)
    B = (p2_x - p1_x)
    C = (p1_x * p2_y - p2_x * p1_y)
    return A, B, -C


def intersection(l_1, l_2):
    D = l_1[0] * l_2[1] - l_1[1] * l_2[0]
    Dx = l_1[2] * l_2[1] - l_1[1] * l_2[2]
    Dy = l_1[0] * l_2[2] - l_1[2] * l_2[0]
    if D != 0:
        x = Dx / D
        y = Dy / D
        return x, y
    else:
        return False


def build_cell_edges(site: Site) -> [Edge]:
    edges = []
    previous_corner = None
    for corner in site.cell_corners:
        if previous_corner is None:
            pass
        else:
            edges.append(Edge(previous_corner[0], previous_corner[1], corner[0], corner[1]))
        previous_corner = corner
    edges.append(Edge(previous_corner[0], previous_corner[1], site.cell_corners[0][0], site.cell_corners[0][1]))
    return edges


def circle_line_segment_intersection(edge_starting_point, edge_ending_point, site: Site, full_line=True,
                                     tangent_tol=1e-9):
    """ Find the points at which a circle intersects a line-segment.  This can happen at 0, 1, or 2 points.

    :param site: The (x, y) location of the circle center
    :param circle_radius: The radius of the circle
    :param edge_starting_point: The (x, y) location of the first point of the segment
    :param edge_ending_point: The (x, y) location of the second point of the segment
    :param full_line: True to find intersections along full line - not just in the segment.  False will just return intersections within the segment.
    :param tangent_tol: Numerical tolerance at which we decide the intersections are close enough to consider it a tangent
    :return Sequence[Tuple[float, float]]: A list of length 0, 1, or 2, where each element is a point at which the circle intercepts a line segment.

    Note: We follow: http://mathworld.wolfram.com/Circle-LineIntersection.html
    """
    (p1x, p1y), (p2x, p2y) = edge_starting_point, edge_ending_point
    cx, cy = site.x, site.y
    (x1, y1), (x2, y2) = (p1x - cx, p1y - cy), (p2x - cx, p2y - cy)
    dx, dy = (x2 - x1), (y2 - y1)
    dr = (dx ** 2 + dy ** 2) ** .5
    big_d = x1 * y2 - x2 * y1
    discriminant = site.coverage_radius ** 2 * dr ** 2 - big_d ** 2

    if discriminant < 0:  # No intersection between circle and line
        return None
    else:  # There may be 0, 1, or 2 intersections with the segment
        intersections = [
            (cx + (big_d * dy + sign * (-1 if dy < 0 else 1) * dx * discriminant ** .5) / dr ** 2,
             cy + (-big_d * dx + sign * abs(dy) * discriminant ** .5) / dr ** 2)
            for sign in ((1, -1) if dy < 0 else (-1, 1))]  # This makes sure the order along the segment is correct
        if not full_line:
            return None
            # If only considering the segment, filter out intersections that do not fall within the segment
            # fraction_along_segment = [(xi - p1x) / dx if abs(dx) > abs(dy) else (yi - p1y) / dy for xi, yi in
            #                           intersections]
            # intersections = [pt for pt, frac in zip(intersections, fraction_along_segment) if 0 <= frac <= 1]
        if len(intersections) == 2 and abs(discriminant) <= tangent_tol:
            return None
        else:
            return intersections


def compute_point_distance(x0, y0, x1, y1):
    return math.sqrt((x1 - x0) ** 2 + (y1 - y0) ** 2)


def compute_circle_segment_area(radius: float, chord_start: [float], chord_end: [float], site: Site):
    chord_len = compute_point_distance(chord_start[0], chord_start[1], chord_end[0], chord_end[1])
    # theta = math.acos((radius ** 2 + radius ** 2 - chord_len ** 2) / (2 * radius ** 2))
    # area = 1 / 2 * (theta - math.sin(theta)) * radius ** 2
    chord_middle = ((chord_end[0] + chord_start[0]) / 2), ((chord_end[1] + chord_start[1]) / 2)
    chord_middle_to_site_dist = compute_point_distance(site.x, site.y, chord_middle[0], chord_middle[1])
    height = site.coverage_radius - chord_middle_to_site_dist
    area = radius ** 2 * math.acos((radius - height) / radius) - (radius - height) * math.sqrt(2 * radius * height - height ** 2)
    return area


def compute_circle_area(radius):
    return radius * radius * math.pi


def compute_polygon_area(cell_corners: [[float]]):
    # Shoelace formula
    n = len(cell_corners)
    area = 0.0
    for i in range(n):
        j = (i + 1) % n
        area += cell_corners[i][0] * cell_corners[j][1]
        area -= cell_corners[j][0] * cell_corners[i][1]
    area = abs(area) / 2.0
    return area


def dot_product(a, b):
    return sum([x * y for x, y in zip(a, b)])


def generate_edges_graph(edges: [Edge]):
    graph = {}
    for edge in edges:
        graph[edge] = []
        for neighbor in edges:
            if edge.find_common_point(neighbor) is not None:
                graph[edge].append(neighbor)
    return graph


def dfs_util(temp, site, visited_sites, graph):
    visited_sites[site] = True
    temp.append(site)
    for neighbor in graph[site]:
        if not visited_sites[neighbor]:
            temp = dfs_util(temp, neighbor, visited_sites, graph)
    return temp


def list_connected_edges_group(edges: [Edge]):
    graph = generate_edges_graph(edges)
    connected_sites = []
    visited_sites = {}
    for edge in edges:
        visited_sites[edge] = False
    for edge in edges:
        if not visited_sites[edge]:
            temp = []
            connected_sites.append(dfs_util(temp, edge, visited_sites, graph))
    return connected_sites
