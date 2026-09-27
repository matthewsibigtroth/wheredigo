# 🥾 Whered I Go — 3D Topographic Android Hiking Tracker

A modern, high-performance, single-screen Android hiking tracker application built from scratch with **Kotlin**, **Jetpack Compose (Material 3)**, **Mapbox Maps SDK v11 (3D Terrain & Outdoors Topo)**, **Google Play Services Fused Location**, and an uninterrupted **Android 14+ Foreground Service**.

---

## 🌟 Features & Architecture

- **Mapbox 3D Topographic Terrain**:
  - Uses the official `com.mapbox.extension:maps-compose` SDK (v11+).
  - Renders the Mapbox **Outdoors Style** (`mapbox://styles/mapbox/outdoors-v12`) with contour lines, elevation markers, and trail paths.
  - Applies **3D Terrain DEM** (`mapbox://mapbox.mapbox-terrain-dem-v1`) with elevation exaggeration for realistic mountain visualization.
  - Initial camera pitch set to a dramatic **60-degree angle** for 3D perspective.
  - Real-time GPS path drawing using Mapbox **`PolylineAnnotation`**.
  - Dynamic user location puck and smooth camera follow mode.

- **Uninterrupted Background GPS Tracking**:
  - **`HikingService`**: Foreground Service supporting **Android 14+ (API 34)** with `android:foregroundServiceType="location"`.
  - Continuous tracking with `FusedLocationProviderClient` (`PRIORITY_HIGH_ACCURACY`, 3–5 second updates) and partial WakeLock.
  - Persistent workout notification with live stats (`Time`, `Distance`, `Calories`, `Altitude`) and a direct **"STOP HIKE"** notification action.

- **Realistic MET Calorie Calculation & Custom Weight (lbs)**:
  - Scientific Metabolic Equivalent of Task (MET) model based on the Compendium of Physical Activities and Minetti/Pandolf equations.
  - **Custom Weight in Pounds (lbs)**: Tap the weight chip in the top metrics card to configure your weight with quick adjustment buttons (`-5`, `-1`, `+1`, `+5 lbs`). Persists across sessions.
  - Dynamically calculates slope/grade (`elevation_gain / distance * 100`) and movement speed to scale calorie burn realistically during uphill ascents.

- **Polished Material 3 Compose UI**:
  - **Top Metrics Card**: Frosted glass semi-transparent card with a 2x2 grid displaying:
    - ⏱️ **Time Elapsed**: `HH:MM:SS`
    - 📏 **Distance**: `X.XX mi`
    - ⛰️ **Altitude**: `X,XXX ft (X,XXX m)`
    - 🔥 **Calories**: `XXX kcal`
    - Live recording indicator, speed, and elevation gain.
  - **Bottom Action Button**: Massive prominent pill-shaped button:
    - Idle: Vibrant Emerald Green (`START HIKE`) with Play icon.
    - Active: Crimson Red (`STOP`) with Stop icon.
  - **Recenter Map Button**: Smoothly animates camera back to user's location.

---

## 🔑 Mapbox Token Configuration

The project is already pre-configured with your Mapbox tokens:

| Token Type | Value | Where Configured |
| :--- | :--- | :--- |
| **Public Token** | `pk.YOUR_MAPBOX_PUBLIC_TOKEN` | `local.properties` |
| **Secret Token** | `sk.YOUR_MAPBOX_SECRET_TOKEN` | `local.properties` |

### `local.properties` setup
```properties
MAPBOX_PUBLIC_TOKEN=pk.YOUR_MAPBOX_PUBLIC_TOKEN
MAPBOX_DOWNLOADS_TOKEN=sk.YOUR_MAPBOX_SECRET_TOKEN
```

### `settings.gradle.kts` Mapbox Maven configuration
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = java.net.URI("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox" // Must always be "mapbox"
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                    .orNull
                    ?: java.util.Properties().apply {
                        val localProps = java.io.File(rootDir, "local.properties")
                        if (localProps.exists()) load(localProps.inputStream())
                    }.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: ""
            }
        }
    }
}
```

---

## 📁 Project Structure

```
wheredigo/
├── build.gradle.kts                      # Root Gradle build script
├── settings.gradle.kts                   # Mapbox Maven repository & module includes
├── gradle.properties                     # JVM & Mapbox download token properties
├── local.properties                      # Secret token configuration
├── gradle/
│   ├── libs.versions.toml                # Version Catalog (Compose, Mapbox, AGP)
│   └── wrapper/gradle-wrapper.properties # Gradle 8.9 wrapper
└── app/
    ├── build.gradle.kts                  # App-level dependencies & Compose configuration
    ├── proguard-rules.pro                # ProGuard rules for Mapbox
    └── src/main/
        ├── AndroidManifest.xml           # Location & Foreground Service permissions
        ├── java/com/wheredigo/hikingtracker/
        │   ├── MainActivity.kt           # Edge-to-edge Compose host Activity
        │   ├── HikingApp.kt              # Application class initializing Mapbox SDK
        │   ├── data/
        │   │   ├── HikingModels.kt       # Data classes: HikingMetrics, HikingSessionState
        │   │   └── HikingRepository.kt   # Singleton state manager with reactive StateFlow
        │   ├── service/
        │   │   └── HikingService.kt      # Android 14+ Foreground Service with GPS & Notification
        │   ├── ui/
        │   │   ├── HikingViewModel.kt    # MVVM ViewModel with StateFlow for Compose UI
        │   │   ├── screens/
        │   │   │   └── HikingTrackerScreen.kt # Single-screen layout container
        │   │   ├── components/
        │   │   │   ├── MapboxHikingMap.kt     # Mapbox Compose 3D map, DEM & polyline trail
        │   │   │   ├── MetricsOverlay.kt      # Frosted glass 2x2 grid stats card
        │   │   │   ├── TrackingControls.kt    # Prominent Start/Stop pill button & recenter FAB
        │   │   │   └── PermissionHandler.kt   # Runtime permission requests & rationale
        │   │   └── theme/
        │   │       ├── Color.kt          # Outdoor green, cyan & amber color scheme
        │   │       ├── Theme.kt          # Dark theme with edge-to-edge system bars
        │   │       └── Type.kt           # Typography definitions
        │   └── utils/
        │       ├── CalorieCalculator.kt  # MET formula factoring in speed and elevation gain
        │       ├── Formatters.kt         # HH:MM:SS, mi, altitude, and kcal formatters
        │       └── LocationUtils.kt      # GPS filtering, distance and GeoJSON converters
        └── res/
            ├── values/
            │   ├── strings.xml           # Public token & UI text resources
            │   ├── colors.xml            # Color resources
            │   └── themes.xml            # Transparent system bar theme
            └── drawable/
                └── ic_hiking_notification.xml # Custom vector notification icon
```

---

## 🚀 How to Run the App

1. Open **Android Studio** (Koala, Ladybug, Iguana, or Hedgehog).
2. Select **Open** and choose the `/Users/sibigtroth/Desktop/projects/personal/wheredigo` folder.
3. Allow Gradle to sync dependencies from Google, MavenCentral, and the Mapbox private Maven repo.
4. Select an Android device or emulator running **Android 8.0 (API 26) through Android 14/15 (API 34+)**.
5. Click **Run** (`Shift + F10`).
6. Grant Location & Notification permissions when prompted, and tap **START HIKE** to begin tracking!
