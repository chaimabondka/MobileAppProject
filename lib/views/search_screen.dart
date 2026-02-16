import 'package:event_booking_app/utils/extensions.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:get/get.dart';

import '../controllers/events_controller.dart';
import '../controllers/search_controller.dart' as ctrl;
import '../models/event.dart';
import '../utils/exports/manager_exports.dart';
import '../utils/exports/widgets_exports.dart';

import 'package:intl/intl.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  static const String routeName = '/searchEventScreen';

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final searchController = Get.put(ctrl.SearchController());
  final eventController = Get.put(EventController());
  @override
  void dispose() {
    Get.delete<ctrl.SearchController>();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: ColorManager.scaffoldBackgroundColor,
      body: Obx(() {
        if (searchController.isLoading) {
          return const Center(
            child: Padding(
              padding: EdgeInsets.all(20.0),
              child: CircularProgressIndicator(),
            ),
          );
        } else if (searchController.searchedEvents.isEmpty) {
          return SingleChildScrollView(
            child: Container(
              margin:
                  const EdgeInsets.symmetric(horizontal: MarginManager.marginM),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Txt(
                    textAlign: TextAlign.start,
                    text: StringsManager.searchEventTxt,
                    fontWeight: FontWeightManager.bold,
                    fontSize: FontSize.headerFontSize,
                    fontFamily: FontsManager.fontFamilyPoppins,
                  ),
                  const SizedBox(
                    height: 12,
                  ),
                  CustomSearchWidget(
                    onChanged: (value) {
                      searchController.onSearchChanged(value.trim());
                    },
                    onFieldSubmit: (value) {
                      searchController.searchEvent(value.trim());
                    },
                  ),
                  Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        const SizedBox(height: SizeManager.sizeXL * 7),
                        SvgPicture.asset(
                          'assets/images/search.svg',
                          height: SizeManager.splashIconSize,
                          width: SizeManager.splashIconSize,
                          fit: BoxFit.scaleDown,
                        ),
                        const SizedBox(
                          height: 20,
                        ),
                        const Txt(
                          textAlign: TextAlign.center,
                          text: StringsManager.searchNowEventTxt,
                          fontWeight: FontWeightManager.bold,
                          fontSize: FontSize.titleFontSize,
                          fontFamily: FontsManager.fontFamilyPoppins,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          );
        } else {
          return SingleChildScrollView(
            child: Container(
              margin:
                  const EdgeInsets.symmetric(horizontal: MarginManager.marginM),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Txt(
                    textAlign: TextAlign.start,
                    text: StringsManager.searchEventTxt,
                    fontWeight: FontWeightManager.bold,
                    fontSize: FontSize.headerFontSize,
                    fontFamily: FontsManager.fontFamilyPoppins,
                  ),
                  const SizedBox(
                    height: 12,
                  ),
                  CustomSearchWidget(
                    onChanged: (value) {
                      searchController.onSearchChanged(value.trim());
                    },
                    onFieldSubmit: (value) {
                      searchController.searchEvent(value.trim());
                    },
                  ),
                  ListView.builder(
                    shrinkWrap: true,
                    itemCount: searchController.searchedEvents.length,
                    itemBuilder: (context, index) {
                      Event event = searchController.searchedEvents[index];
                      return isEventOngoing(event)
                          ? InkWell(
                              onTap: () {
                                showModalBottomSheet(
                                    context: context,
                                    builder: (BuildContext context) {
                                      return CustomBottomSheet(event: event);
                                    });
                              },
                              child: Container(
                                margin: const EdgeInsets.symmetric(
                                    vertical: MarginManager.marginM),
                                padding: const EdgeInsets.all(
                                    PaddingManager.paddingM),
                                decoration: BoxDecoration(
                                  color: ColorManager.cardBackGroundColor,
                                  borderRadius: BorderRadius.circular(10),
                                ),
                                child: Row(
                                  children: [
                                    Container(
                                      width: 80,
                                      height: 80,
                                      decoration: BoxDecoration(
                                        borderRadius: BorderRadius.circular(10),
                                        color: ColorManager.primaryColor,
                                      ),
                                      child: ClipRRect(
                                        borderRadius: BorderRadius.circular(10),
                                        child: Image.network(
                                          event.posterUrl,
                                          fit: BoxFit.cover,
                                          errorBuilder:
                                              (context, error, stackTrace) {
                                            return const Icon(Icons.error);
                                          },
                                          loadingBuilder: (context, child,
                                              loadingProgress) {
                                            if (loadingProgress == null)
                                              return child;
                                            return const Center(
                                                child:
                                                    CircularProgressIndicator());
                                          },
                                        ),
                                      ),
                                    ),
                                    const SizedBox(
                                      width: 12,
                                    ),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        children: [
                                          Txt(
                                            text: event.name,
                                            textAlign: TextAlign.start,
                                            fontWeight: FontWeightManager.bold,
                                            fontSize:
                                                FontSize.titleFontSize * 0.7,
                                            fontFamily:
                                                FontsManager.fontFamilyPoppins,
                                            color: ColorManager.blackColor,
                                          ),
                                          const SizedBox(
                                            height: 4,
                                          ),
                                          Txt(
                                            text:
                                                '${event.startDate} at ${event.startTime}',
                                            textAlign: TextAlign.start,
                                            fontWeight:
                                                FontWeightManager.regular,
                                            fontSize:
                                                FontSize.titleFontSize * 0.6,
                                            fontFamily:
                                                FontsManager.fontFamilyPoppins,
                                            color: ColorManager.secondaryColor,
                                          ),
                                        ],
                                      ),
                                    ),
                                    const SizedBox(
                                      width: 12,
                                    ),
                                  ],
                                ),
                              ),
                            )
                          : Container();
                    },
                  ),
                ],
              ),
            ),
          );
        }
      }),
    );
  }

  bool isEventOngoing(Event event) {
    final endDate = DateFormat("dd-MM-yyyy").parse(event.endDate);
    final endTime =
        TimeOfDay.fromDateTime(DateFormat("h:mm a").parse(event.endTime))
            .toDateTime();
    final now = DateTime.now();

    return endDate.isAfter(now) ||
        (endDate.isAtSameMomentAs(now) && endTime.isAfter(now));
  }
}
