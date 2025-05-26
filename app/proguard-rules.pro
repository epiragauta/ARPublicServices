# Keep classes for ARCore
-keep class com.google.ar.** { *; }
-keep class com.google.ar.sceneform.** { *; }

# Keep classes for Maps
-keep class com.google.android.gms.maps.** { *; }

# Keep model classes
-keep class com.example.arpublicservices.PublicService { *; }
-keep class com.example.arpublicservices.MapBounds { *; }

# Keep JSON parsing
-keepattributes Signature
-keepattributes *Annotation*
-keep class org.json.** { *; }
