// services/route_service.dart
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../models/route_plan.dart';
import '../models/bus_stop.dart';

class RouteService {
  static const String GOOGLE_MAPS_API_KEY = 'YOUR_GOOGLE_MAPS_API_KEY';
  static const String API_BASE_URL = 'http://localhost:8080/api';
  
  static Future<List<RoutePlan>> findBestRoutes(
      double fromLat, double fromLng, double toLat, double toLng) async {
    
    try {
      // First, check for direct routes
      final directRoutes = await _findDirectRoutes(fromLat, fromLng, toLat, toLng);
      
      if (directRoutes.isNotEmpty) {
        return directRoutes;
      }
      
      // If no direct routes, find transfer routes
      return await _findTransferRoutes(fromLat, fromLng, toLat, toLng);
      
    } catch (e) {
      print('Error finding routes: $e');
      return [];
    }
  }
  
  static Future<List<RoutePlan>> _findDirectRoutes(
      double fromLat, double fromLng, double toLat, double toLng) async {
    
    final response = await http.get(
      Uri.parse('$API_BASE_URL/routes/direct?fromLat=$fromLat&fromLng=$fromLng&toLat=$toLat&toLng=$toLng'),
    );
    
    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((json) => RoutePlan.fromJson(json)).toList();
    }
    
    return [];
  }
  
  static Future<List<RoutePlan>> _findTransferRoutes(
      double fromLat, double fromLng, double toLat, double toLng) async {
    
    final response = await http.get(
      Uri.parse('$API_BASE_URL/routes/transfer?fromLat=$fromLat&fromLng=$fromLng&toLat=$toLat&toLng=$toLng'),
    );
    
    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((json) => RoutePlan.fromJson(json)).toList();
    }
    
    return [];
  }
  
  static Future<Map<String, dynamic>> getGoogleMapsDirections(
      double fromLat, double fromLng, double toLat, double toLng) async {
    
    final String url = 'https://maps.googleapis.com/maps/api/directions/json'
        '?origin=$fromLat,$fromLng'
        '&destination=$toLat,$toLng'
        '&mode=transit'
        '&key=$GOOGLE_MAPS_API_KEY';
    
    final response = await http.get(Uri.parse(url));
    
    if (response.statusCode == 200) {
      return json.decode(response.body);
    }
    
    throw Exception('Failed to get directions');
  }
}

// models/route_plan.dart
class RoutePlan {
  final String id;
  final List<RouteSegment> segments;
  final int totalDurationMinutes;
  final double totalDistanceKm;
  final int transferCount;
  final double estimatedFare;

  RoutePlan({
    required this.id,
    required this.segments,
    required this.totalDurationMinutes,
    required this.totalDistanceKm,
    required this.transferCount,
    required this.estimatedFare,
  });

  factory RoutePlan.fromJson(Map<String, dynamic> json) {
    return RoutePlan(
      id: json['id'],
      segments: (json['segments'] as List)
          .map((s) => RouteSegment.fromJson(s))
          .toList(),
      totalDurationMinutes: json['totalDurationMinutes'],
      totalDistanceKm: json['totalDistanceKm'].toDouble(),
      transferCount: json['transferCount'],
      estimatedFare: json['estimatedFare'].toDouble(),
    );
  }
}

class RouteSegment {
  final String type; // 'bus', 'walk'
  final String? busNumber;
  final String? routeName;
  final BusStop? startStop;
  final BusStop? endStop;
  final int durationMinutes;
  final double distanceKm;
  final List<LatLng> polylinePoints;

  RouteSegment({
    required this.type,
    this.busNumber,
    this.routeName,
    this.startStop,
    this.endStop,
    required this.durationMinutes,
    required this.distanceKm,
    required this.polylinePoints,
  });

  factory RouteSegment.fromJson(Map<String, dynamic> json) {
    return RouteSegment(
      type: json['type'],
      busNumber: json['busNumber'],
      routeName: json['routeName'],
      startStop: json['startStop'] != null ? BusStop.fromJson(json['startStop']) : null,
      endStop: json['endStop'] != null ? BusStop.fromJson(json['endStop']) : null,
      durationMinutes: json['durationMinutes'],
      distanceKm: json['distanceKm'].toDouble(),
      polylinePoints: (json['polylinePoints'] as List)
          .map((point) => LatLng(point['lat'].toDouble(), point['lng'].toDouble()))
          .toList(),
    );
  }
}

// models/bus_stop.dart
class BusStop {
  final String id;
  final String name;
  final double latitude;
  final double longitude;
  final List<String> busRoutes;

  BusStop({
    required this.id,
    required this.name,
    required this.latitude,
    required this.longitude,
    required this.busRoutes,
  });

  factory BusStop.fromJson(Map<String, dynamic> json) {
    return BusStop(
      id: json['id'],
      name: json['name'],
      latitude: json['latitude'].toDouble(),
      longitude: json['longitude'].toDouble(),
      busRoutes: List<String>.from(json['busRoutes']),
    );
  }
}
