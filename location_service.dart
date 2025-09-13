// services/location_service.dart
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class LocationService extends ChangeNotifier {
  Position? _currentPosition;
  LatLng? _currentLocation;
  bool _isLocationServiceEnabled = false;
  bool _hasPermission = false;
  LocationPermission? _permission;
  
  Position? get currentPosition => _currentPosition;
  LatLng? get currentLocation => _currentLocation;
  bool get isLocationServiceEnabled => _isLocationServiceEnabled;
  bool get hasPermission => _hasPermission;
  
  Future<void> initialize() async {
    await _checkLocationService();
    await _requestPermission();
    if (_hasPermission && _isLocationServiceEnabled) {
      await getCurrentLocation();
      _startLocationTracking();
    }
  }
  
  Future<void> _checkLocationService() async {
    _isLocationServiceEnabled = await Geolocator.isLocationServiceEnabled();
    notifyListeners();
  }
  
  Future<void> _requestPermission() async {
    _permission = await Geolocator.checkPermission();
    
    if (_permission == LocationPermission.denied) {
      _permission = await Geolocator.requestPermission();
    }
    
    if (_permission == LocationPermission.deniedForever) {
      _hasPermission = false;
    } else if (_permission == LocationPermission.denied) {
      _hasPermission = false;
    } else {
      _hasPermission = true;
    }
    
    notifyListeners();
  }
  
  Future<void> getCurrentLocation() async {
    if (!_hasPermission || !_isLocationServiceEnabled) return;
    
    try {
      _currentPosition = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );
      
      if (_currentPosition != null) {
        _currentLocation = LatLng(
          _currentPosition!.latitude,
          _currentPosition!.longitude,
        );
        notifyListeners();
      }
    } catch (e) {
      print('Error getting current location: $e');
    }
  }
  
  void _startLocationTracking() {
    Geolocator.getPositionStream(
      locationSettings: LocationSettings(
        accuracy: LocationAccuracy.high,
        distanceFilter: 10, // Update every 10 meters
      ),
    ).listen((Position position) {
      _currentPosition = position;
      _currentLocation = LatLng(position.latitude, position.longitude);
      notifyListeners();
    });
  }
  
  double getDistanceToLocation(double latitude, double longitude) {
    if (_currentPosition == null) return 0.0;
    
    return Geolocator.distanceBetween(
      _currentPosition!.latitude,
      _currentPosition!.longitude,
      latitude,
      longitude,
    );
  }
  
  Future<void> openLocationSettings() async {
    await Geolocator.openLocationSettings();
  }
  
  Future<void> openAppSettings() async {
    await Geolocator.openAppSettings();
  }
}
