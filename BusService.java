// service/BusService.java
package com.citybusapp.service;

import com.citybusapp.model.Bus;
import com.citybusapp.repository.BusRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusService {
    
    @Autowired
    private BusRepository busRepository;
    
    private final DatabaseReference firebaseRef;
    
    public BusService() {
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("buses");
    }
    
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }
    
    public List<Bus> searchByNumber(String number) {
        return busRepository.findByNumberContainingIgnoreCase(number);
    }
    
    public Bus getBusById(String id) {
        return busRepository.findById(id).orElse(null);
    }
    
    public void updateBusLocation(String busId, Double latitude, Double longitude, Double speed) {
        Bus bus = busRepository.findById(busId).orElse(null);
        if (bus != null) {
            bus.setLatitude(latitude);
            bus.setLongitude(longitude);
            bus.setSpeed(speed);
            bus.setLastUpdated(LocalDateTime.now());
            
            // Save to PostgreSQL
            busRepository.save(bus);
            
            // Update Firebase for real-time tracking
            updateFirebaseLocation(bus);
        }
    }
    
    private void updateFirebaseLocation(Bus bus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("id", bus.getId());
        updates.put("number", bus.getNumber());
        updates.put("routeName", bus.getRouteName());
        updates.put("latitude", bus.getLatitude());
        updates.put("longitude", bus.getLongitude());
        updates.put("speed", bus.getSpeed());
        updates.put("occupancy", bus.getOccupancy());
        updates.put("lastUpdated", bus.getLastUpdated().toString());
        
        firebaseRef.child(bus.getId()).setValueAsync(updates);
    }
    
    // Simulate bus movement for demo purposes
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void simulateBusMovement() {
        List<Bus> buses = getAllBuses();
        for (Bus bus : buses) {
            // Simple random movement for demo
            double latOffset = (Math.random() - 0.5) * 0.001;
            double lngOffset = (Math.random() - 0.5) * 0.001;
            
            updateBusLocation(bus.getId(), 
                            bus.getLatitude() + latOffset,
                            bus.getLongitude() + lngOffset,
                            20 + Math.random() * 40);
        }
    }
}
