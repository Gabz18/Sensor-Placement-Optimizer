import math
import random
import copy

from model import Site, Edge


def generate_random_solution_b(nb_sensor: int, sensor_coverage_distance: float, x_plane_size, y_plane_size):
    min_distance_between_sites = sensor_coverage_distance * 0.75
    sites = []
    for i in range(nb_sensor):
        min_distance_is_satisfied = False
        while not min_distance_is_satisfied:
            site = Site(random.uniform(0, x_plane_size), random.uniform(0, y_plane_size), sensor_coverage_distance)
            if len(sites) > 0:
                previous_point = sites[len(sites) - 1]
                point_distance = compute_point_distance(previous_point.x, previous_point.y, site.x, site.y)
                if min_distance_between_sites <= point_distance <= sensor_coverage_distance:
                    min_distance_is_satisfied = True
            else:
                min_distance_is_satisfied = True

        sites.append(site)

    return sites


def generate_random_solution(nb_sensor: int, sensor_coverage_distance: float, x_plane_size, y_plane_size):
    sites = []
    for i in range(nb_sensor):
        realistic = False
        while not realistic:
            site = Site(random.uniform(0, x_plane_size), random.uniform(0, y_plane_size), sensor_coverage_distance)
            if len(sites) > 0:
                temp = [copy.copy(site) for site in sites]
                temp.append(site)
                realistic = solution_is_realistic(temp)
            else:
                realistic = True

        sites.append(site)

    return sites


def solution_is_realistic(sites: [Site]) -> bool:
    """
    Ensures that a solution is realistic -> the sensor graph is connected
    """
    graph = generate_solution_graph(sites)
    connected_sites = []
    visited_sites = {}
    for site in sites:
        visited_sites[site] = False
    for site in sites:
        if not visited_sites[site]:
            temp = []
            connected_sites.append(dfs_util(temp, site, visited_sites, graph))
    if len(connected_sites) == 1:
        return True
    return False


def dfs_util(temp, site, visited_sites, graph):
    visited_sites[site] = True
    temp.append(site)
    for neighbor in graph[site]:
        if not visited_sites[neighbor]:
            temp = dfs_util(temp, neighbor, visited_sites, graph)
    return temp


def generate_solution_graph(sites: [Site]):
    graph = {}
    for site in sites:
        graph[site] = []
        for neighbor in sites:
            if compute_point_distance(site.x, site.y, neighbor.x, neighbor.y) <= site.coverage_radius:
                graph[site].append(neighbor)
    return graph


def compute_point_distance(x0, y0, x1, y1):
    return math.sqrt((x1 - x0) ** 2 + (y1 - y0) ** 2)


if __name__ == "__main__":
    generate_random_solution_b(10, 100, 500, 500)

