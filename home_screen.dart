import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:provider/provider.dart';
import '../services/bus_service.dart';
import '../services/location_service.dart';
import '../models/bus.dart';
import '../models/tourist_spot.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  GoogleMapController? _controller;
  Set<Marker> _markers = {};
  Set<Polyline> _polylines = {};
  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
    _initializeServices();
  }

  void _initializeServices() {
    Provider.of<LocationService>(context, listen: false).getCurrentLocation();
    Provider.of<BusService>(context, listen: false).startListeningToBuses();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('City Bus & Tourism'),
        backgroundColor: Colors.blueAccent,
        actions: [
          IconButton(
            icon: Icon(Icons.search),
            onPressed: _showBusSearch,
          ),
        ],
      ),
      body: Consumer2<BusService, LocationService>(
        builder: (context, busService, locationService, child) {
          _updateMarkers(busService.buses, busService.touristSpots);
          
          return GoogleMap(
            onMapCreated: (GoogleMapController controller) {
              _controller = controller;
            },
            initialCameraPosition: CameraPosition(
              target: locationService.currentLocation ?? 
                     LatLng(16.5062, 80.6480), // Guntur coordinates
              zoom: 13.0,
            ),
            markers: _markers,
            polylines: _polylines,
            myLocationEnabled: true,
            myLocationButtonEnabled: true,
          );
        },
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: _onBottomNavTapped,
        items: [
          BottomNavigationBarItem(
            icon: Icon(Icons.directions_bus),
            label: 'Live Buses',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.place),
            label: 'Tourist Spots',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.route),
            label: 'My Routes',
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showRouteOptions,
        child: Icon(Icons.navigation),
        backgroundColor: Colors.orange,
      ),
    );
  }

  void _updateMarkers(List<Bus> buses, List<TouristSpot> spots) {
    _markers.clear();
    
    // Add bus markers
    for (Bus bus in buses) {
      _markers.add(
        Marker(
          markerId: MarkerId('bus_${bus.id}'),
          position: LatLng(bus.latitude, bus.longitude),
          icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueBlue),
          infoWindow: InfoWindow(
            title: 'Bus ${bus.number}',
            snippet: 'Route: ${bus.routeName}\nSpeed: ${bus.speed} km/h',
          ),
        ),
      );
    }

    // Add tourist spot markers
    if (_selectedIndex == 1) {
      for (TouristSpot spot in spots) {
        _markers.add(
          Marker(
            markerId: MarkerId('spot_${spot.id}'),
            position: LatLng(spot.latitude, spot.longitude),
            icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueGreen),
            infoWindow: InfoWindow(
              title: spot.name,
              snippet: spot.description,
            ),
            onTap: () => _showTouristSpotDetails(spot),
          ),
        );
      }
    }
  }

  void _showBusSearch() {
    showDialog(
      context: context,
      builder: (context) => BusSearchDialog(),
    );
  }

  void _showTouristSpotDetails(TouristSpot spot) {
    showModalBottomSheet(
      context: context,
      builder: (context) => TouristSpotDetails(spot: spot),
    );
  }

  void _onBottomNavTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
    
    if (index == 1) {
      Provider.of<BusService>(context, listen: false).loadTouristSpots();
    }
  }

  void _showRouteOptions() {
    showModalBottomSheet(
      context: context,
      builder: (context) => RouteOptionsSheet(),
    );
  }
}
