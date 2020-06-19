import math
import subprocess
import time
import copy

from model import Site
from sensor_coverage_calculator import compute_solution_plane_coverage_percentage
from solution_evaluator import generate_random_solution, solution_is_realistic, generate_random_solution_b


def voronoi_relaxation(sites: [Site], x_plane_size, y_plane_size, max_iteration: int) -> int:
    i = 0
    previous_result = None
    best_realistic_result = None
    best_realistic_result_solution = None
    while i < 100:

        sites = generate_voronoi_and_get_cells(x_plane_size, y_plane_size, sites)
        realistic = solution_is_realistic(sites)
        result = compute_solution_plane_coverage_percentage(sites, x_plane_size, y_plane_size)
        if realistic:
            if best_realistic_result is None or result > best_realistic_result:
                best_realistic_result = result
                best_realistic_result_solution = [copy.copy(site) for site in sites]
        if i >= max_iteration and realistic:
            print("Iteration " + str(i) + ", coverage = ", str(result), ", realistic : ", str(realistic))
            break
        print("Iteration " + str(i) + ", coverage = ", str(result), ", realistic : ", str(realistic))
        for site in sites:
            update_site_pos_with_cell_centroid(site)
        i += 1

    if best_realistic_result is not None:
        print("Best realistic result : ", str(best_realistic_result))
        print("Best realistic result solution : ", [site.to_string() for site in best_realistic_result_solution])
        # Display best result
        subprocess_call = ['java', '-jar', './VoronoiGenerator.jar',
                           '-r', str(sites[0].coverage_radius),
                           '-h', str(y_plane_size),
                           '-w', str(x_plane_size)]
        for site in sites:
            subprocess_call.append('-s')
            subprocess_call.append(site.to_string())
        subprocess.Popen(subprocess_call, stdout=subprocess.PIPE)

    else:
        print("No realistic solution found")
    return i


def generate_voronoi_and_get_cells(x_plane_size: float, y_plane_size: float, sites: [Site]):
    subprocess_call = ['java', '-jar', './VoronoiGenerator.jar', '--no-display', '-h', str(y_plane_size), '-w',
                       str(x_plane_size)]
    for site in sites:
        subprocess_call.append('-s')
        subprocess_call.append(site.to_string())

    res = []
    proc = subprocess.Popen(subprocess_call, stdout=subprocess.PIPE)
    """
    a = ""
    for i in subprocess_call:
        a += i + " "
    print(a)
    """
    while True:
        line = proc.stdout.readline()
        if not line:
            break
        res.append(parse_site_cell(str(line.rstrip()), sites[0].coverage_radius))
    return res


def parse_site_cell(line: str, coverage_radius: float):
    s = line[2:].split("|")
    point = s[0]
    corners = s[1][1:len(s[1]) - 2]

    p = point.split(":")
    site_x = p[0][1:]
    site_y = p[1][:len(p[1]) - 1]

    str_points = corners.split(", ")
    points = []
    for point in str_points:
        coordinates = point[1:len(point) - 1].split(":")
        points.append((float(coordinates[0]), float(coordinates[1])))

    return Site(float(site_x), float(site_y), coverage_radius, points)


def update_site_pos_with_cell_centroid(site: Site):
    centroid = polygon_centre_area(site.cell_corners)
    updated_pos = find_weighted_updated_site_location((site.x, site.y), centroid, WEIGHT)
    site.x = updated_pos[0]
    site.y = updated_pos[1]


def polygon_centre_area(vertices: [[float]]):
    x_cent = y_cent = area = 0
    v_local = vertices + [vertices[0]]

    for i in range(len(v_local) - 1):
        factor = v_local[i][0] * v_local[i + 1][1] - v_local[i + 1][0] * v_local[i][1]
        area += factor
        x_cent += (v_local[i][0] + v_local[i + 1][0]) * factor
        y_cent += (v_local[i][1] + v_local[i + 1][1]) * factor

    area /= 2.0
    x_cent /= (6 * area)
    y_cent /= (6 * area)
    return x_cent, y_cent


def find_weighted_updated_site_location(initial_point: [float], target_point: [float], weight: float):
    vector = target_point[0] - initial_point[0], target_point[1] - initial_point[1]
    vector_distance = math.sqrt((target_point[0] - initial_point[0]) ** 2 + (target_point[1] - initial_point[1]) ** 2)
    u = vector[0] / vector_distance, vector[1] / vector_distance
    weighted_point = initial_point[0] + u[0] * vector_distance * weight, initial_point[1] + u[
        1] * vector_distance * weight
    return weighted_point


if __name__ == "__main__":
    WEIGHT = 1
    coverage_radius = 100
    start = time.time()
    # iterations = voronoi_relaxation(generate_random_solution(34, 100, 500, 500), 500, 500, 15)
    sites = [
        Site(356.19560084901246, 127.42318033479866, coverage_radius),
        Site(418.7012015568561, 81.20783302622547, coverage_radius),
        Site(282.2647422336893, 139.04607145486347, coverage_radius),
        Site(462.71824390613375, 138.61894677935322, coverage_radius),
        Site(495.8338855452489, 50.272726680936685, coverage_radius),
        Site(374.599719019584, 108.28343578447591, coverage_radius),
        Site(372.24584006522247, 129.03846814142344, coverage_radius),
        Site(406.17483780539385, 140.24310280444857, coverage_radius),
        Site(466.5597007333434, 61.28632635196157, coverage_radius),
        Site(233.796901623617, 120.66404604823155, coverage_radius)
    ]
    iterations = voronoi_relaxation(sites, 500, 500, 20)
    print(str(time.time() - start) + " seconds to run for " + str(iterations) + " iterations.")
