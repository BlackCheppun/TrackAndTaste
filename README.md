
# Restaurant Tracking App - Track And Taste 

## Project Overview
Track And Taste is an Android application developed as part of a university project, designed for restaurant tracking, reminders, and easy navigation. Users can set reminders for their favorite restaurants, receive notifications, and use gestures to open a navigation route to a restaurant. The app integrates local data storage and location services to provide a seamless user experience.

### Contributors

**Ilyes AGHOUILES**, ilyes.aghouiles@etu.cyu.fr

**Karine MOUSSAOUI ,** Karine.moussaoui@etu.cyu.fr

**Course: UE MOBILE COMPUTING**

**Institution: Cergy University of Paris**

**Degree: Licence 3 Informatique**

### Features
- **Restaurant Tracking**: Keep track of your favorite restaurants with easy access to their details.
- **Reminders**: Set daily or one-time reminders for restaurants you love.
- **Gesture Detection**: Detects shaking and navigates to the map.
- **Dark Mode**: Switch between light and dark modes for a personalized experience.
- **Map Navigation**: Integration with Open route Service for seamless location based navigation.

### Technologies and Dependencies
***Android SDK & Plugins***

*Android Studio*: has to support AGP 8.7, use Latest version if possible, current latest version : [LadyBug](https://developer.android.com/studio/releases?hl=fr).

*SDK Version*:

- **compileSdk**: 34

- **minSdk**: 34

- **targetSdk**: 34

Reason for choosing such versions is because ORS and OSM are not compatible in older versions with newer versions of android.

### **Libraries & Dependencies**
- **OSMDroid (for UI map integration)**: version 6.1.15 [OpenStreetMap](https://www.openstreetmap.org/about)
- **Open route service ( search and navigation ) : [OpenRouteService](https://openrouteservice.org/)**

## Setup & Installation 

1.  Clone the repository
```
git clone https://github.com/BlackCheppun/TrackAndTaste
```

3.  Open the project in Android Studio.

4.  Install dependencies:
- Ensure your environment is set up to use Android SDK version 34.

- Sync the Gradle files to ensure all libraries and dependencies are installed.

**API Keys & Configuration**

**Map Integration**: OSM is opensource and as such, no further steps are needed, but to use ORS, an API key is needed. ORS provides API keys for free with limited usage for personal projects; limited to 2000 direction queries a day, and 1000 search queries a day. For the sake of the project, a personal key is used when performing geocoding/directions. Kindly visit [openrouteservice](https://openrouteservice.org/)[ ](https://openrouteservice.org/)to get an API key, in case one is needed for developpement.

## App Usage 
- **Navigation**: Users can track their favorite restaurants and set navigation preferences to get quick access to them via OpenStreetMap.
- **Reminders**: Set a daily reminder for a restaurant, and receive notificationsat a custom time.
- **Shake Gesture**: Enable the gesture service to open the map route to your restaurant when a shaking gesture is detected.
  
# **Version History**

## [V1.0]- 2024/12/23 : 
Initial release with basic functionality like restaurant tracking, setting reminders, and using gestures for navigation.

