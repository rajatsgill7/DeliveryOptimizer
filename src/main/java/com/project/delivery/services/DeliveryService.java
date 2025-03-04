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

    public Mono<String> processDeliveryRequest(DeliveryRequest request) {
        Map<String, double[]> locations = extractLocations(request);
        double restaurant1PreparationTime = request.getRestaurant1PreparationTime();
        double restaurant2PreparationTime = request.getRestaurant2PreparationTime();

        logger.info("Starting A* search for the optimal delivery route...");
        List<String> bestRoute = findOptimalRouteAStar(locations, restaurant1PreparationTime, restaurant2PreparationTime);

        if (bestRoute.isEmpty()) {
            logger.error("A* search failed: No valid route found!");
            return Mono.just("Error: No valid delivery route found.");
        }

        double estimatedTime = calculateTotalTravelTime(bestRoute, locations, restaurant1PreparationTime, restaurant2PreparationTime);
        logger.info("Optimal Route Found: {} | Estimated Time: {} hours", bestRoute, estimatedTime);

        return Mono.just(String.format("Environment: %s | Best route: %s, Estimated Time: %.2f hours",
                appConfig.getEnvironment(), String.join(" -> ", bestRoute), estimatedTime));
    }

    private Map<String, double[]> extractLocations(DeliveryRequest request) {
        Map<String, double[]> locations = new HashMap<>();
        locations.put("DeliveryHub", new double[]{request.getHubLatitude(), request.getHubLongitude()});
        locations.put("Restaurant1", new double[]{request.getRestaurant1Latitude(), request.getRestaurant1Longitude()});
        locations.put("Restaurant2", new double[]{request.getRestaurant2Latitude(), request.getRestaurant2Longitude()});
        locations.put("Customer1", new double[]{request.getCustomer1Latitude(), request.getCustomer1Longitude()});
        locations.put("Customer2", new double[]{request.getCustomer2Latitude(), request.getCustomer2Longitude()});
        return locations;
    }

    private List<String> findOptimalRouteAStar(Map<String, double[]> locations, double pt1, double pt2) {
        PriorityQueue<RouteNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.estimatedTotalCost));
        Set<Set<String>> visitedStates = new HashSet<>();

        RouteNode startNode = new RouteNode("DeliveryHub", new LinkedHashSet<>(List.of("DeliveryHub")), 0, heuristic("DeliveryHub", locations, new HashSet<>()), new HashSet<>());
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            RouteNode current = openSet.poll();
            logger.debug("Processing Node: {} | Path: {} | Travel Cost: {} | Estimated Total Cost: {}", current.location, current.path, current.travelCost, current.estimatedTotalCost);

            if (current.visitedLocations.size() == locations.size()) {
                logger.info("Finalized Best Route: {}", current.path);
                return new ArrayList<>(current.path);
            }

            for (String nextLocation : locations.keySet()) {
                if (current.visitedLocations.contains(nextLocation) || !isNextStepValid(nextLocation, current.visitedLocations)) {
                    continue;
                }

                LinkedHashSet<String> newPath = new LinkedHashSet<>(current.path);
                newPath.add(nextLocation);
                Set<String> newVisitedLocations = new HashSet<>(current.visitedLocations);
                newVisitedLocations.add(nextLocation);

                double travelTime = computeTravelTime(locations.get(current.location), locations.get(nextLocation));
                if (nextLocation.equals("Restaurant1")) travelTime = Math.max(pt1, travelTime);
                if (nextLocation.equals("Restaurant2")) travelTime = Math.max(pt2, travelTime);

                double travelCost = current.travelCost + travelTime;
                double estimatedTotalCost = travelCost + heuristic(nextLocation, locations, newVisitedLocations);

                if (!visitedStates.contains(newVisitedLocations)) {
                    openSet.add(new RouteNode(nextLocation, newPath, travelCost, estimatedTotalCost, newVisitedLocations));
                    visitedStates.add(newVisitedLocations);
                }
            }
        }
        return new ArrayList<>();
    }

    private boolean isNextStepValid(String next, Set<String> visited) {
        return !(next.equals("Customer1") && !visited.contains("Restaurant1")) && !(next.equals("Customer2") && !visited.contains("Restaurant2"));
    }

    private double heuristic(String point, Map<String, double[]> locations, Set<String> visited) {
        return locations.keySet().stream().filter(loc -> !visited.contains(loc)).mapToDouble(loc ->
                computeHaversineDistance(locations.get(point), locations.get(loc))
        ).sum() / AVERAGE_SPEED_KMH;
    }

    private double calculateTotalTravelTime(List<String> route, Map<String, double[]> locations, double pt1, double pt2) {
        double totalTime = 0;
        double[] prevLocation = locations.get(route.get(0));

        for (int i = 1; i < route.size(); i++) {
            double[] currLocation = locations.get(route.get(i));
            double travelTime = computeTravelTime(prevLocation, currLocation);

            if (route.get(i).equals("Restaurant1")) travelTime = Math.max(pt1, travelTime);
            if (route.get(i).equals("Restaurant2")) travelTime = Math.max(pt2, travelTime);

            totalTime += travelTime;
            prevLocation = currLocation;
        }
        return totalTime;
    }

    private double computeTravelTime(double[] start, double[] end) {
        return computeHaversineDistance(start, end) / AVERAGE_SPEED_KMH;
    }

    private double computeHaversineDistance(double[] start, double[] end) {
        double dLat = Math.toRadians(end[0] - start[0]);
        double dLon = Math.toRadians(end[1] - start[1]);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(start[0])) * Math.cos(Math.toRadians(end[0])) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    static class RouteNode {
        String location;
        LinkedHashSet<String> path;
        double travelCost;
        double estimatedTotalCost;
        Set<String> visitedLocations;

        public RouteNode(String location, LinkedHashSet<String> path, double travelCost, double estimatedTotalCost, Set<String> visitedLocations) {
            this.location = location;
            this.path = path;
            this.travelCost = travelCost;
            this.estimatedTotalCost = estimatedTotalCost;
            this.visitedLocations = visitedLocations;
        }
    }
}
