// controller/RouteController.java
package com.citybusapp.controller;

import com.citybusapp.model.RoutePlan;
import com.citybusapp.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {
    
    @Autowired
    private RouteService routeService;
    
    @GetMapping("/direct")
    public ResponseEntity<List<RoutePlan>> findDirectRoutes(
            @RequestParam Double fromLat,
            @RequestParam Double fromLng,
            @RequestParam Double toLat,
            @RequestParam Double toLng) {
        
        List<RoutePlan> routes = routeService.findDirectRoutes(fromLat, fromLng, toLat, toLng);
        return ResponseEntity.ok(routes);
    }
    
    @GetMapping("/transfer")
    public ResponseEntity<List<RoutePlan>> findTransferRoutes(
            @RequestParam Double fromLat,
            @RequestParam Double fromLng,
            @RequestParam Double toLat,
            @RequestParam Double toLng) {
        
        List<RoutePlan> routes = routeService.findTransferRoutes(fromLat, fromLng, toLat, toLng);
        return ResponseEntity.ok(routes);
    }
    
    @GetMapping("/stops/nearby")
    public ResponseEntity<List<BusStop>> findNearbyStops(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "0.5") Double radiusKm) {
        
        List<BusStop> stops = routeService.findNearbyBusStops(latitude, longitude, radiusKm);
        return ResponseEntity.ok(stops);
    }
}

// service/RouteService.java
package com.citybusapp.service;

import com.citybusapp.model.*;
import com.citybusapp.repository.BusStopRepository;
import com.citybusapp.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {
    
    @Autowired
    private BusStopRepository busStopRepository;
    
    @Autowired
    private RouteRepository routeRepository;
    
    public List<RoutePlan> findDirectRoutes(Double fromLat, Double fromLng, 
                                          Double toLat, Double toLng) {
        List<RoutePlan> directRoutes = new ArrayList<>();
        
        // Find nearby stops for origin and destination
        List<BusStop> originStops = findNearbyBusStops(fromLat, fromLng, 0.5);
        List<BusStop> destStops = findNearbyBusStops(toLat, toLng, 0.5);
        
        // Check for common routes between origin and destination stops
        for (BusStop originStop : originStops) {
            for (BusStop destStop : destStops) {
                List<String> commonRoutes = findCommonRoutes(originStop, destStop);
                
                for (String routeNumber : commonRoutes) {
                    RoutePlan plan = createDirectRoutePlan(originStop, destStop, routeNumber);
                    if (plan != null) {
                        directRoutes.add(plan);
                    }
                }
            }
        }
        
        return directRoutes.stream()
                .sorted((r1, r2) -> Integer.compare(r1.getTotalDurationMinutes(), r2.getTotalDurationMinutes()))
                .limit(3)
                .collect(Collectors.toList());
    }
    
    public List<RoutePlan> findTransferRoutes(Double fromLat, Double fromLng, 
                                            Double toLat, Double toLng) {
        List<RoutePlan> transferRoutes = new ArrayList<>();
        
        // Find nearby stops
        List<BusStop> originStops = findNearbyBusStops(fromLat, fromLng, 0.5);
        List<BusStop> destStops = findNearbyBusStops(toLat, toLng, 0.5);
        
        // Find intermediate transfer points
        for (BusStop originStop : originStops) {
            for (BusStop destStop : destStops) {
                List<RoutePlan> routes = findTransferRoutesWithOneTransfer(originStop, destStop);
                transferRoutes.addAll(routes);
            }
        }
        
        return transferRoutes.stream()
                .sorted((r1, r2) -> Integer.compare(r1.getTotalDurationMinutes(), r2.getTotalDurationMinutes()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    public List<BusStop> findNearbyBusStops(Double latitude, Double longitude, Double radiusKm) {
        return busStopRepository.findAll().stream()
                .filter(stop -> calculateDistance(latitude, longitude, 
                                                stop.getLatitude(), stop.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }
    
    private List<String> findCommonRoutes(BusStop stop1, BusStop stop2) {
        return stop1.getBusRoutes().stream()
                .filter(route -> stop2.getBusRoutes().contains(route))
                .collect(Collectors.toList());
    }
    
    private RoutePlan createDirectRoutePlan(BusStop originStop, BusStop destStop, String routeNumber) {
        List<RouteSegment> segments = new ArrayList<>();
        
        // Walking segment to origin stop
        RouteSegment walkToOrigin = new RouteSegment();
        walkToOrigin.setType("walk");
        walkToOrigin.setDurationMinutes(3);
        walkToOrigin.setDistanceKm(0.2);
        segments.add(walkToOrigin);
        
        // Bus segment
        RouteSegment busSegment = new RouteSegment();
        busSegment.setType("bus");
        busSegment.setBusNumber(routeNumber);
        busSegment.setRouteName(getRouteNameByNumber(routeNumber));
        busSegment.setStartStop(originStop);
        busSegment.setEndStop(destStop);
        busSegment.setDurationMinutes(calculateBusTravelTime(originStop, destStop));
        busSegment.setDistanceKm(calculateDistance(originStop.getLatitude(), originStop.getLongitude(),
                                                  destStop.getLatitude(), destStop.getLongitude()));
        segments.add(busSegment);
        
        // Walking segment from destination stop
        RouteSegment walkFromDest = new RouteSegment();
        walkFromDest.setType("walk");
        walkFromDest.setDurationMinutes(2);
        walkFromDest.setDistanceKm(0.1);
        segments.add(walkFromDest);
        
        RoutePlan plan = new RoutePlan();
        plan.setId("direct_" + System.currentTimeMillis());
        plan.setSegments(segments);
        plan.setTotalDurationMinutes(segments.stream().mapToInt(RouteSegment::getDurationMinutes).sum());
        plan.setTotalDistanceKm(segments.stream().mapToDouble(RouteSegment::getDistanceKm).sum());
        plan.setTransferCount(0);
        plan.setEstimatedFare(15.0); // Base fare
        
        return plan;
    }
    
    private List<RoutePlan> findTransferRoutesWithOneTransfer(BusStop originStop, BusStop destStop) {
        List<RoutePlan> routes = new ArrayList<>();
        
        // Find potential transfer stops
        List<BusStop> allStops = busStopRepository.findAll();
        
        for (BusStop transferStop : allStops) {
            // Check if there's a route from origin to transfer stop
            List<String> firstLegRoutes = findCommonRoutes(originStop, transferStop);
            // Check if there's a route from transfer stop to destination
            List<String> secondLegRoutes = findCommonRoutes(transferStop, destStop);
            
            if (!firstLegRoutes.isEmpty() && !secondLegRoutes.isEmpty()) {
                RoutePlan transferRoute = createTransferRoutePlan(
                    originStop, transferStop, destStop,
                    firstLegRoutes.get(0), secondLegRoutes.get(0));
                if (transferRoute != null) {
                    routes.add(transferRoute);
                }
            }
        }
        
        return routes;
    }
    
    private RoutePlan createTransferRoutePlan(BusStop originStop, BusStop transferStop, 
                                            BusStop destStop, String firstRoute, String secondRoute) {
        List<RouteSegment> segments = new ArrayList<>();
        
        // Walk to origin stop
        segments.add(createWalkSegment(3, 0.2));
        
        // First bus segment
        RouteSegment firstBus = new RouteSegment();
        firstBus.setType("bus");
        firstBus.setBusNumber(firstRoute);
        firstBus.setRouteName(getRouteNameByNumber(firstRoute));
        firstBus.setStartStop(originStop);
        firstBus.setEndStop(transferStop);
        firstBus.setDurationMinutes(calculateBusTravelTime(originStop, transferStop));
        firstBus.setDistanceKm(calculateDistance(originStop.getLatitude(), originStop.getLongitude(),
                                               transferStop.getLatitude(), transferStop.getLongitude()));
        segments.add(firstBus);
        
        // Transfer walk
        segments.add(createWalkSegment(5, 0.1));
        
        // Second bus segment
        RouteSegment secondBus = new RouteSegment();
        secondBus.setType("bus");
        secondBus.setBusNumber(secondRoute);
        secondBus.setRouteName(getRouteNameByNumber(secondRoute));
        secondBus.setStartStop(transferStop);
        secondBus.setEndStop(destStop);
        secondBus.setDurationMinutes(calculateBusTravelTime(transferStop, destStop));
        secondBus.setDistanceKm(calculateDistance(transferStop.getLatitude(), transferStop.getLongitude(),
                                                destStop.getLatitude(), destStop.getLongitude()));
        segments.add(secondBus);
        
        // Walk from destination stop
        segments.add(createWalkSegment(2, 0.1));
        
        RoutePlan plan = new RoutePlan();
        plan.setId("transfer_" + System.currentTimeMillis());
        plan.setSegments(segments);
        plan.setTotalDurationMinutes(segments.stream().mapToInt(RouteSegment::getDurationMinutes).sum());
        plan.setTotalDistanceKm(segments.stream().mapToDouble(RouteSegment::getDistanceKm).sum());
        plan.setTransferCount(1);
        plan.setEstimatedFare(25.0); // Base fare + transfer fee
        
        return plan;
    }
    
    private RouteSegment createWalkSegment(int duration, double distance) {
        RouteSegment segment = new RouteSegment();
        segment.setType("walk");
        segment.setDurationMinutes(duration);
        segment.setDistanceKm(distance);
        return segment;
    }
    
    private String getRouteNameByNumber(String routeNumber) {
        // This would typically query the database
        // For demo purposes, return a sample route name
        return "Route " + routeNumber;
    }
    
    private int calculateBusTravelTime(BusStop from, BusStop to) {
        double distance = calculateDistance(from.getLatitude(), from.getLongitude(),
                                          to.getLatitude(), to.getLongitude());
        // Assume average speed of 25 km/h in city traffic
        return Math.max(5, (int) (distance / 25.0 * 60));
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS = 6371; // kilometers
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
