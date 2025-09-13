import 'package:flutter/foundation.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../models/bus.dart';
import '../models/tourist_spot.dart';

class BusService extends ChangeNotifier {
  static const String API_BASE_URL = 'http://localhost:8080/api';
  
  List<Bus> _buses = [];
  List<TouristSpot> _touristSpots = [];
  List<Bus> _searchResults = [];
  Bus? _selectedBus;
  
  final DatabaseReference _busRef = FirebaseDatabase.instance.ref('buses');
  
  List<Bus> get buses => _buses;
  List<TouristSpot> get touristSpots => _touristSpots;
  List<Bus> get searchResults => _searchResults;
  Bus? get selectedBus => _selectedBus;

  void startListeningToBuses() {
    _busRef.onValue.listen((event) {
      final data = event.snapshot.value as Map<dynamic, dynamic>?;
      if (data != null) {
        _buses = data.entries.map((entry) {
          final busData = entry.value as Map<dynamic, dynamic>;
          return Bus.fromJson(Map<String, dynamic>.from(busData));
        }).toList();
        notifyListeners();
      }
    });
  }

  Future<void> searchBus(String busNumber) async {
    try {
      final response = await http.get(
        Uri.parse('$API_BASE_URL/buses/search?number=$busNumber'),
      );
      
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(response.body);
        _searchResults = data.map((json) => Bus.fromJson(json)).toList();
        notifyListeners();
      }
    } catch (e) {
      print('Error searching bus: $e');
    }
  }

  void selectBus(Bus bus) {
    _selectedBus = bus;
    notifyListeners();
  }

  Future<void> loadTouristSpots() async {
    try {
      final response = await http.get(
        Uri.parse('$API_BASE_URL/tourist-spots'),
      );
      
      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(response.body);
        _touristSpots = data.map((json) => TouristSpot.fromJson(json)).toList();
        notifyListeners();
      }
    } catch (e) {
      print('Error loading tourist spots: $e');
    }
  }

  Future<List<Map<String, dynamic>>> getRouteToDestination(
    double fromLat, double fromLng, double toLat, double toLng) async {
    try {
      final response = await http.get(
        Uri.parse('$API_BASE_URL/routes/find?fromLat=$fromLat&fromLng=$fromLng&toLat=$toLat&toLng=$toLng'),
      );
      
      if (response.statusCode == 200) {
        return List<Map<String, dynamic>>.from(json.decode(response.body));
      }
    } catch (e) {
      print('Error getting route: $e');
    }
    return [];
  }
}
