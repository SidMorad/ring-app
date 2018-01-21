# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /i/program/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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


-keep class mars.ring.** { *; }
-dontnote mars.ring.**

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.Platform$Java8
-dontnote retrofit2.Platform

# OkHttp3
-dontwarn okhttp3.**
-dontnote okhttp3.**

-dontnote sun.misc.Unsafe
-dontnote com.google.gson.internal.UnsafeAllocator

# Guava
-keep class com.google.gson.reflect.TypeToken
-dontwarn sun.misc.Unsafe
-dontwarn javax.lang.model.element.Modifier
-dontnote com.google.appengine.api.**
-dontnote com.google.apphosting.api.**
-dontnote com.google.gson.**

# AltBeacon library
-dontnote org.altbeacon.beacon.SimulatedScanData

# Google play services
-dontnote com.google.android.gms.**

# Other
-dontnote android.net.http.*
-dontnote org.apache.http.**
-dontwarn java.lang.ClassValue