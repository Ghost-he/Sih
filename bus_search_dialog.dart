// widgets/bus_search_dialog.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/bus_service.dart';

class BusSearchDialog extends StatefulWidget {
  @override
  _BusSearchDialogState createState() => _BusSearchDialogState();
}

class _BusSearchDialogState extends State<BusSearchDialog> {
  final _searchController = TextEditingController();
  
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Search Bus'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _searchController,
            decoration: InputDecoration(
              hintText: 'Enter bus number (e.g., B101)',
              prefixIcon: Icon(Icons.search),
            ),
          ),
          SizedBox(height: 20),
          Consumer<BusService>(
            builder: (context, busService, child) {
              if (busService.searchResults.isNotEmpty) {
                return Container(
                  height: 200,
                  child: ListView.builder(
                    itemCount: busService.searchResults.length,
                    itemBuilder: (context, index) {
                      final bus = busService.searchResults[index];
                      return ListTile(
                        leading: Icon(Icons.directions_bus),
                        title: Text('Bus ${bus.number}'),
                        subtitle: Text('Route: ${bus.routeName}'),
                        onTap: () {
                          Navigator.pop(context);
                          busService.selectBus(bus);
                        },
                      );
                    },
                  ),
                );
              }
              return Container();
            },
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text('Cancel'),
        ),
        TextButton(
          onPressed: () {
            Provider.of<BusService>(context, listen: false)
                .searchBus(_searchController.text);
          },
          child: Text('Search'),
        ),
      ],
    );
  }
}
