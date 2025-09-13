// models/bus.dart
class Bus {
  final String id;
  final String number;
  final String routeName;
  final double latitude;
  final double longitude;
  final double speed;
  final int occupancy;
  final DateTime lastUpdated;

  Bus({
    required this.id,
    required this.number,
    required this.routeName,
    required this.latitude,
    required this.longitude,
    required this.speed,
    required this.occupancy,
    required this.lastUpdated,
  });

  factory Bus.fromJson(Map<String, dynamic> json) {
    return Bus(
      id: json['id'],
      number: json['number'],
      routeName: json['routeName'],
      latitude: json['latitude'].toDouble(),
      longitude: json['longitude'].toDouble(),
      speed: json['speed'].toDouble(),
      occupancy: json['occupancy'],
      lastUpdated: DateTime.parse(json['lastUpdated']),
    );
  }
}

// models/tourist_spot.dart
class TouristSpot {
  final String id;
  final String name;
  final String description;
  final double latitude;
  final double longitude;
  final String category;
  final double rating;
  final String imageUrl;

  TouristSpot({
    required this.id,
    required this.name,
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.category,
    required this.rating,
    required this.imageUrl,
  });

  factory TouristSpot.fromJson(Map<String, dynamic> json) {
    return TouristSpot(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      latitude: json['latitude'].toDouble(),
      longitude: json['longitude'].toDouble(),
      category: json['category'],
      rating: json['rating'].toDouble(),
      imageUrl: json['imageUrl'] ?? '',
    );
  }
}
