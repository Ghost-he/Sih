// model/BusStop.java
package com.citybusapp.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "bus_stops")
public class BusStop {
    @Id
    private String id;
    
    private String name;
    private Double latitude;
    private Double longitude;
    
    @ElementCollection
    @CollectionTable(name = "bus_stop_routes", joinColumns = @JoinColumn(name = "stop_id"))
    @Column(name = "route_number")
    private List<String> busRoutes;
    
    // Constructors, getters, setters
    public BusStop() {}
    
    public BusStop(String id, String name, Double latitude, Double longitude, List<String> busRoutes) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.busRoutes = busRoutes;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public List<String> getBusRoutes() { return busRoutes; }
    public void setBusRoutes(List<String> busRoutes) { this.busRoutes = busRoutes; }
}

// model/RoutePlan.java
package com.citybusapp.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "route_plans")
public class RoutePlan {
    @Id
    private String id;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "route_plan_id")
    private List<RouteSegment> segments;
    
    private Integer totalDurationMinutes;
    private Double totalDistanceKm;
    private Integer transferCount;
    private Double estimatedFare;
    
    // Constructors, getters, setters
    public RoutePlan() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public List<RouteSegment> getSegments() { return segments; }
    public void setSegments(List<RouteSegment> segments) { this.segments = segments; }
    
    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Integer totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
    
    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    
    public Integer getTransferCount() { return transferCount; }
    public void setTransferCount(Integer transferCount) { this.transferCount = transferCount; }
    
    public Double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(Double estimatedFare) { this.estimatedFare = estimatedFare; }
}

// model/RouteSegment.java
package com.citybusapp.model;

import javax.persistence.*;

@Entity
@Table(name = "route_segments")
public class RouteSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String type; // 'bus' or 'walk'
    private String busNumber;
    private String routeName;
    
    @ManyToOne
    @JoinColumn(name = "start_stop_id")
    private BusStop startStop;
    
    @ManyToOne
    @JoinColumn(name = "end_stop_id")
    private BusStop endStop;
    
    private Integer durationMinutes;
    private Double distanceKm;
    
    // Constructors, getters, setters
    public RouteSegment() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    
    public BusStop getStartStop() { return startStop; }
    public void setStartStop(BusStop startStop) { this.startStop = startStop; }
    
    public BusStop getEndStop() { return endStop; }
    public void setEndStop(BusStop endStop) { this.endStop = endStop; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}

// model/TouristSpot.java
package com.citybusapp.model;

import javax.persistence.*;

@Entity
@Table(name = "tourist_spots")
public class TouristSpot {
    @Id
    private String id;
    
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String category;
    private Double rating;
    private String imageUrl;
    
    // Constructors, getters, setters
    public TouristSpot() {}
    
    public TouristSpot(String id, String name, String description, Double latitude, 
                      Double longitude, String category, Double rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
