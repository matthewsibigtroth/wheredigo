# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/.../android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Mapbox ProGuard rules
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**
