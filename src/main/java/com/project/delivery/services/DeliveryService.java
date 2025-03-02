package com.project.delivery.services;

import com.project.delivery.config.Config;
import com.project.delivery.dto.DeliveryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private static final double AVERAGE_SPEED_KMH = 20.0;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private final Config appConfig;

    public DeliveryService(Config appConfig) {
        this.appConfig = appConfig;
    }

    public Mono<String> processRequest(DeliveryRequest request) {
        // Store all locations
        Map<String, double[]> locations = new HashMap<>();
        locations.put("Aman", new double[]{request.getAmanLat(), request.getAmanLon()});
        locations.put("R1", new double[]{request.getR1Lat(), request.getR1Lon()});
        locations.put("R2", new double[]{request.getR2Lat(), request.getR2Lon()});
        locations.put("C1", new double[]{request.getC1Lat(), request.getC1Lon()});
        locations.put("C2", new double[]{request.getC2Lat(), request.getC2Lon()});

        double pt1 = request.getPt1();
        double pt2 = request.getPt2();

        logger.info("Starting A* search for the optimal delivery route...");

        List<String> bestRoute = findOptimalRouteAStar(locations, pt1, pt2);

        if (bestRoute.isEmpty()) {
            logger.error("A* search failed: No valid route found! This should never happen.");
            return Mono.just("Error: No valid delivery route found.");
        }

        double estimatedTime = calculateTotalTime(bestRoute, locations, pt1, pt2);
        logger.info("Optimal Route Found: {} | Estimated Time: {} hours", bestRoute, estimatedTime);

        return Mono.just("Environment: " + appConfig.getEnvironment() + " | Best route: " +
                String.join(" -> ", bestRoute) + ", Estimated Time: " + estimatedTime + " hours");
    }

    private List<String> findOptimalRouteAStar(Map<String, double[]> locations, double pt1, double pt2) {
        PriorityQueue<RouteNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Set<Set<String>> visitedStates = new HashSet<>(); // Track visited node sets to prevent revisits

        // Start from Aman
        RouteNode startNode = new RouteNode("Aman", new LinkedHashSet<>(List.of("Aman")), 0, heuristic("Aman", locations, new HashSet<>()), new HashSet<>());
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            RouteNode current = openSet.poll();
            logger.debug("Processing Node: {} | Path: {} | gScore: {} | fScore: {}", current.point, current.path, current.gScore, current.fScore);

            // If all locations are visited, return the route
            if (current.visited.size() == locations.size()) {
                logger.info("Finalized Best Route: {}", current.path);
                return new ArrayList<>(current.path);
            }

            for (String next : locations.keySet()) {
                if (current.visited.contains(next) || !isValidNextStep(next, current.visited)) {
                    logger.debug("Skipping {} due to constraints.", next);
                    continue;
                }

                // Generate multiple orderings dynamically
                LinkedHashSet<String> newPath = new LinkedHashSet<>(current.path);
                newPath.add(next);

                Set<String> newVisited = new HashSet<>(current.visited);
                newVisited.add(next);

                double travelTime = travelTime(locations.get(current.point)[0], locations.get(current.point)[1], locations.get(next)[0], locations.get(next)[1]);

                // Apply restaurant preparation time correctly
                if (next.equals("R1")) travelTime = Math.max(pt1, travelTime);
                if (next.equals("R2")) travelTime = Math.max(pt2, travelTime);

                double gScore = current.gScore + travelTime;
                double fScore = gScore + heuristic(next, locations, newVisited);

                logger.debug("Adding new path: {} | gScore: {} | fScore: {}", newPath, gScore, fScore);

                // Prevent revisiting same node sets
                if (!visitedStates.contains(newVisited)) {
                    openSet.add(new RouteNode(next, newPath, gScore, fScore, newVisited));
                    visitedStates.add(newVisited);
                }
            }
        }

        logger.warn("No valid path found! This should not happen.");
        return new ArrayList<>();
    }


    private boolean isValidNextStep(String next, Set<String> visited) {
        if (next.equals("C1") && !visited.contains("R1")) {
            logger.debug("Skipping {} because R1 has not been visited yet.", next);
            return false;
        }
        if (next.equals("C2") && !visited.contains("R2")) {
            logger.debug("Skipping {} because R2 has not been visited yet.", next);
            return false;
        }
        return true;
    }

    private double heuristic(String point, Map<String, double[]> locations, Set<String> visited) {
        double[] currentLoc = locations.get(point);
        double minDistance = 0.0;

        for (String loc : locations.keySet()) {
            if (!visited.contains(loc)) {
                double[] targetLoc = locations.get(loc);
                minDistance += haversine(currentLoc[0], currentLoc[1], targetLoc[0], targetLoc[1]);
            }
        }
        return minDistance / AVERAGE_SPEED_KMH;
    }

    private double calculateTotalTime(List<String> route, Map<String, double[]> locations, double pt1, double pt2) {
        double totalTime = 0;
        double prevLat = locations.get(route.get(0))[0];
        double prevLon = locations.get(route.get(0))[1];

        for (int i = 1; i < route.size(); i++) {
            double[] currLocation = locations.get(route.get(i));
            double travelTime = travelTime(prevLat, prevLon, currLocation[0], currLocation[1]);

            if (route.get(i).equals("R1")) travelTime = Math.max(pt1, travelTime);
            if (route.get(i).equals("R2")) travelTime = Math.max(pt2, travelTime);

            totalTime += travelTime;
            prevLat = currLocation[0];
            prevLon = currLocation[1];
        }
        return totalTime;
    }

    private double travelTime(double lat1, double lon1, double lat2, double lon2) {
        double distance = haversine(lat1, lon1, lat2, lon2);
        return distance / AVERAGE_SPEED_KMH;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    static class RouteNode {
        String point;
        LinkedHashSet<String> path;
        double gScore;
        double fScore;
        Set<String> visited;

        public RouteNode(String point, LinkedHashSet<String> path, double gScore, double fScore, Set<String> visited) {
            this.point = point;
            this.path = path;
            this.gScore = gScore;
            this.fScore = fScore;
            this.visited = visited;
        }
    }
}
