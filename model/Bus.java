package com.citybusapp.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "buses")
public class Bus {
    @Id
    private String id;
    
    @Column(unique = true)
    private String number;
    
    private String routeName;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Integer occupancy;
    private LocalDateTime lastUpdated;
    
    // Constructors, getters, setters
    public Bus() {}
    
    public Bus(String id, String number, String routeName, 
               Double latitude, Double longitude, Double speed, 
               Integer occupancy, LocalDateTime lastUpdated) {
        this.id = id;
        this.number = number;
        this.routeName = routeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.occupancy = occupancy;
        this.lastUpdated = lastUpdated;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    
    public Integer getOccupancy() { return occupancy; }
    public void setOccupancy(Integer occupancy) { this.occupancy = occupancy; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}

// controller/BusController.java
package com.citybusapp.controller;

import com.citybusapp.model.Bus;
import com.citybusapp.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@CrossOrigin(origins = "*")
public class BusController {
    
    @Autowired
    private BusService busService;
    
    @GetMapping
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Bus>> searchBuses(@RequestParam String number) {
        return ResponseEntity.ok(busService.searchByNumber(number));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Bus> getBusById(@PathVariable String id) {
        Bus bus = busService.getBusById(id);
        return bus != null ? ResponseEntity.ok(bus) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/location")
    public ResponseEntity<Void> updateBusLocation(
            @PathVariable String id,
            @RequestBody LocationUpdateRequest request) {
        busService.updateBusLocation(id, request.getLatitude(), 
                                   request.getLongitude(), request.getSpeed());
        return ResponseEntity.ok().build();
    }
    
    public static class LocationUpdateRequest {
        private Double latitude;
        private Double longitude;
        private Double speed;
        
        // Getters and setters
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Double getSpeed() { return speed; }
        public void setSpeed(Double speed) { this.speed = speed; }
    }
}
