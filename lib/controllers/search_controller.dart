import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:event_booking_app/manager/firebase_constants.dart';
import 'package:event_booking_app/models/event.dart';
import 'package:get/get.dart';

class SearchController extends GetxController {
  final Rx<List<Event>> _searchedEvents = Rx<List<Event>>([]);
  final Rx<bool> _isLoading = false.obs;

  List<Event> get searchedEvents => _searchedEvents.value;
  bool get isLoading => _isLoading.value;

  Future<void> searchEvent(String typedUser) async {
    if (typedUser.isEmpty) {
      _searchedEvents.value = [];
      return;
    }

    _isLoading.value = true;
    List<Event> retVal = [];

    try {
      // Solution 1: Recherche simple sans filtre Firestore
      QuerySnapshot query = await firestore.collection('events').get();

      for (var elem in query.docs) {
        Event event = await Event.fromSnap(elem);
        // Filtrage côté client (plus fiable)
        if (event.name.toLowerCase().contains(typedUser.toLowerCase()) ||
            event.category.toLowerCase().contains(typedUser.toLowerCase()) ||
            event.description.toLowerCase().contains(typedUser.toLowerCase())) {
          retVal.add(event);
        }
      }

      if (retVal.isEmpty) {
        Get.snackbar(
          'No results',
          'No events found matching "$typedUser"',
        );
      }
    } catch (e) {
      Get.snackbar(
        'Search Error',
        'Failed to search: $e',
      );
    } finally {
      _isLoading.value = false;
      _searchedEvents.value = retVal;
    }
  }

  // Recherche en temps réel avec debounce
  void onSearchChanged(String value) {
    if (value.isEmpty) {
      _searchedEvents.value = [];
      return;
    }

    // Debounce pour éviter trop de requêtes
    Future.delayed(const Duration(milliseconds: 500), () {
      if (value.isNotEmpty) {
        searchEvent(value.trim());
      }
    });
  }
}
