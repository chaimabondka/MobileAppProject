import 'package:event_booking_app/routes/app_pages.dart';
import 'package:event_booking_app/routes/app_routes.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:get_storage/get_storage.dart';

import 'manager/strings_manager.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Firebase for all platforms
  try {
    await Firebase.initializeApp(
      options: FirebaseOptions(
          apiKey: "AIzaSyCGPAfDnE39p6WNvWG6Qo6NhUHrzqhgqwk",
          authDomain: "event-booking-app-81f2f.firebaseapp.com",
          projectId: "event-booking-app-81f2f",
          storageBucket: "event-booking-app-81f2f.firebasestorage.app",
          messagingSenderId: "13776980974",
          appId: "1:13776980974:web:46d1633f411a95a298dcff",
          measurementId: "G-RB6TR1Z5VT"),
    );
  } catch (e) {
    print('Firebase initialization failed: $e');
  }

  await GetStorage.init();
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      key: UniqueKey(),
      debugShowCheckedModeBanner: false,
      title: StringsManager.appName,
      smartManagement: SmartManagement.full,
      defaultTransition: Transition.fade,
      initialRoute: AppRoutes.splash,
      getPages: AppPages.pages,
    );
  }
}
