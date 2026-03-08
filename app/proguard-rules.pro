# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities and DAOs
-keep class com.ivarna.adirstat.data.local.db.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.ivarna.adirstat.domain.model.FileNode { *; }
-keep class com.ivarna.adirstat.domain.model.FileNode$File { *; }
-keep class com.ivarna.adirstat.domain.model.FileNode$Directory { *; }
-keep class com.ivarna.adirstat.domain.model.FileCategory { *; }
