// widgets/route_options_sheet.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/route_service.dart';
import '../services/location_service.dart';
import '../models/route_plan.dart';
import '../models/tourist_spot.dart';

class RouteOptionsSheet extends StatefulWidget {
  @override
  _RouteOptionsSheetState createState() => _RouteOptionsSheetState();
}

class _RouteOptionsSheetState extends State<RouteOptionsSheet> {
  final _destinationController = TextEditingController();
  List<RoutePlan> _routePlans = [];
  bool _isLoading = false;
  TouristSpot? _selectedDestination;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.8,
      child: Column(
        children: [
          // Header
          Container(
            padding: EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.blueAccent,
              borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
            ),
            child: Row(
              children: [
                Icon(Icons.route, color: Colors.white),
                SizedBox(width: 12),
                Text(
                  'Plan Your Journey',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Spacer(),
                IconButton(
                  icon: Icon(Icons.close, color: Colors.white),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
          ),
          
          // Destination Input
          Padding(
            padding: EdgeInsets.all(16),
            child: Column(
              children: [
                TextField(
                  controller: _destinationController,
                  decoration: InputDecoration(
                    hintText: 'Enter destination or select tourist spot',
                    prefixIcon: Icon(Icons.location_on),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                ),
                SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: ElevatedButton.icon(
                        onPressed: _showTouristSpotSelector,
                        icon: Icon(Icons.place),
                        label: Text('Tourist Spots'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.green,
                        ),
                      ),
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: ElevatedButton.icon(
                        onPressed: _findRoutes,
                        icon: Icon(Icons.search),
                        label: Text('Find Routes'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.orange,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          
          // Route Results
          Expanded(
            child: _isLoading
                ? Center(child: CircularProgressIndicator())
                : _routePlans.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.directions_bus, size: 64, color: Colors.grey),
                            SizedBox(height: 16),
                            Text(
                              'Enter a destination to find routes',
                              style: TextStyle(color: Colors.grey, fontSize: 16),
                            ),
                          ],
                        ),
                      )
                    : ListView.builder(
                        itemCount: _routePlans.length,
                        itemBuilder: (context, index) {
                          return RouteCard(routePlan: _routePlans[index]);
                        },
                      ),
          ),
        ],
      ),
    );
  }

  void _showTouristSpotSelector() {
    // Implementation for tourist spot selector
    showModalBottomSheet(
      context: context,
      builder: (context) => TouristSpotSelector(
        onSpotSelected: (spot) {
          setState(() {
            _selectedDestination = spot;
            _destinationController.text = spot.name;
          });
          Navigator.pop(context);
        },
      ),
    );
  }

  void _findRoutes() async {
    if (_destinationController.text.isEmpty && _selectedDestination == null) return;
    
    setState(() {
      _isLoading = true;
    });

    final locationService = Provider.of<LocationService>(context, listen: false);
    final currentLocation = locationService.currentLocation;
    
    if (currentLocation != null) {
      double toLat, toLng;
      
      if (_selectedDestination != null) {
        toLat = _selectedDestination!.latitude;
        toLng = _selectedDestination!.longitude;
      } else {
        // For demo purposes, use a default location
        // In production, you'd geocode the address
        toLat = 16.5062;
        toLng = 80.6480;
      }
      
      final routes = await RouteService.findBestRoutes(
        currentLocation.latitude,
        currentLocation.longitude,
        toLat,
        toLng,
      );
      
      setState(() {
        _routePlans = routes;
        _isLoading = false;
      });
    }
  }
}

// Route Card Widget
class RouteCard extends StatelessWidget {
  final RoutePlan routePlan;

  const RouteCard({Key? key, required this.routePlan}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: ExpansionTile(
        leading: CircleAvatar(
          backgroundColor: routePlan.transferCount == 0 ? Colors.green : Colors.orange,
          child: Text(
            routePlan.transferCount == 0 ? 'D' : '${routePlan.transferCount}T',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
          ),
        ),
        title: Text(
          '${routePlan.totalDurationMinutes} min • ₹${routePlan.estimatedFare.toStringAsFixed(0)}',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        subtitle: Text(
          routePlan.transferCount == 0
              ? 'Direct route'
              : '${routePlan.transferCount} transfer${routePlan.transferCount > 1 ? 's' : ''}',
        ),
        children: [
          Padding(
            padding: EdgeInsets.all(16),
            child: Column(
              children: routePlan.segments.map((segment) {
                return RouteSegmentWidget(segment: segment);
              }).toList(),
            ),
          ),
        ],
      ),
    );
  }
}

class RouteSegmentWidget extends StatelessWidget {
  final RouteSegment segment;

  const RouteSegmentWidget({Key? key, required this.segment}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Icon(
            segment.type == 'bus' ? Icons.directions_bus : Icons.directions_walk,
            color: segment.type == 'bus' ? Colors.blue : Colors.grey,
          ),
          SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (segment.type == 'bus') ...[
                  Text('Bus ${segment.busNumber} - ${segment.routeName}',
                      style: TextStyle(fontWeight: FontWeight.w500)),
                  if (segment.startStop != null && segment.endStop != null)
                    Text('${segment.startStop!.name} → ${segment.endStop!.name}',
                        style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                ] else ...[
                  Text('Walk ${(segment.distanceKm * 1000).round()}m',
                      style: TextStyle(fontWeight: FontWeight.w500)),
                ],
                Text('${segment.durationMinutes} min',
                    style: TextStyle(color: Colors.grey[600], fontSize: 12)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
