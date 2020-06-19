import math
from copy import copy


class Site:

    def __init__(self, x, y, coverage_radius: float, cell_corners: [[float, float]] = None) -> None:
        super().__init__()
        self.x = x
        self.y = y
        self.coverage_radius = coverage_radius
        self.cell_corners = cell_corners if cell_corners is not None else []

    def to_string(self) -> str:
        return str(self.x) + ':' + str(self.y)


class Edge:

    def __init__(self, x_start, y_start, x_end, y_end) -> None:
        super().__init__()
        self.x_start = x_start
        self.y_start = y_start
        self.x_end = x_end
        self.y_end = y_end
        self.updated_start = False
        self.updated_end = False
        self.origin_state = copy(self)

    def to_string(self) -> str:
        return str((self.x_start, self.y_start)) + str((self.x_end, self.y_end))

    def find_common_point(self, other: "Edge") -> [float]:
        this_p1 = (self.x_start, self.y_start)
        this_p2 = (self.x_end, self.y_end)
        other_p1 = (other.x_start, other.y_start)
        other_p2 = (other.x_end, other.y_end)
        if this_p1 == other_p1 or this_p1 == other_p2:
            return this_p1
        if this_p2 == other_p1 or this_p2 == other_p2:
            return this_p2
        return None

    def define_edge_point_to_keep(self, site: Site, other_edge: "Edge", edges_intersection_point: [float]):
        """
        This is used in the case where two edges are intersecting themselves inside a circle, and the point that should
        removed from the voronoi cell has to chosen.

        To choose it, we trace one edge going through the site and this edge starting point, and another edge going through
        this edge ending point. The traced edge crossing the other_edge, is traced using the point that should not be kept

        Segment have to be used instead of lines
        """
        if segment_intersects((other_edge.x_start, other_edge.y_start),
                              (other_edge.x_end, other_edge.y_end),
                              (site.x, site.y),
                              (self.x_start, self.y_start)):
            self.x_start = edges_intersection_point[0]
            self.y_start = edges_intersection_point[1]
            self.updated_start = True
        else:
            self.x_end = edges_intersection_point[0]
            self.y_end = edges_intersection_point[1]
            self.updated_end = True


def compute_point_distance(x0, y0, x1, y1):
    return math.sqrt((x1 - x0) ** 2 + (y1 - y0) ** 2)


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


def ccw(a, b, c):
    return (c[1] - a[1]) * (b[0] - a[0]) > (b[1] - a[1]) * (c[0] - a[0])


# Return true if line segments AB and CD intersect
def segment_intersects(a, b, c, d) -> bool:
    return ccw(a, c, d) != ccw(b, c, d) and ccw(a, b, c) != ccw(a, b, d)
