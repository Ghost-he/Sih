package com.citybusapp.controller;

import com.citybusapp.model.TouristSpot;
import com.citybusapp.service.TouristSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tourist-spots")
@CrossOrigin(origins = "*")
public class TouristSpotController {
    
    @Autowired
    private TouristSpotService touristSpotService;
    
    @GetMapping
    public ResponseEntity<List<TouristSpot>> getAllTouristSpots() {
        return ResponseEntity.ok(touristSpotService.getAllTouristSpots());
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<List<TouristSpot>> getNearbySpots(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        return ResponseEntity.ok(
            touristSpotService.findNearbySpots(latitude, longitude, radiusKm));
    }
}

// service/TouristSpotService.java
package com.citybusapp.service;

import com.citybusapp.model.TouristSpot;
import com.citybusapp.repository.TouristSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TouristSpotService {
    
    @Autowired
    private TouristSpotRepository touristSpotRepository;
    
    public List<TouristSpot> getAllTouristSpots() {
        return touristSpotRepository.findAll();
    }
    
    public List<TouristSpot> findNearbySpots(Double latitude, Double longitude, Double radiusKm) {
        return touristSpotRepository.findAll().stream()
            .filter(spot -> calculateDistance(latitude, longitude, 
                                            spot.getLatitude(), spot.getLongitude()) <= radiusKm)
            .collect(Collectors.toList());
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS = 6371; // kilometers
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
