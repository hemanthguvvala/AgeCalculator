# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========== Zodiac Age App ProGuard Rules ==========

# Strip debug logs from release — leaks PII and bloats binary
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep data model classes (Gson is used for content-pack JSON parsing)
-keep class com.hkgroups.agecalculator.data.model.** { *; }
-keepclassmembers class com.hkgroups.agecalculator.data.model.** { *; }

# Keep Room database entities
-keep class com.hkgroups.agecalculator.data.local.** { *; }
-keepclassmembers class com.hkgroups.agecalculator.data.local.** { *; }

# Gson — used only for content-pack JSON parsing
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ---------- Facebook Audience Network ----------
# FAN ships with consumer rules but we keep these as belt-and-braces so
# release builds don't strip required reflection targets.
-keep class com.facebook.ads.** { *; }
-keep interface com.facebook.ads.** { *; }
-keep class com.facebook.infer.annotation.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**

# ---------- Google Play Billing ----------
# Billing client uses reflection on its callback proxies; without the keep
# rule, release-mode obfuscation breaks the purchase flow silently.
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ---------- Glance (home-screen widget) ----------
# Glance's RemoteViews translation uses reflection on Composable functions.
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# ---------- Google UMP (User Messaging Platform / GDPR consent) ----------
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# ---------- App-internal: WorkManager + Hilt workers ----------
-keep class com.hkgroups.agecalculator.worker.** { *; }
-keep class com.hkgroups.agecalculator.widget.** { *; }
-keep class com.hkgroups.agecalculator.util.FanAdsController { *; }
-keep class com.hkgroups.agecalculator.util.BillingController { *; }