# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Firebase rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Room database rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class com.pseddev.playstreak.data.entities.** { *; }
-keep class com.pseddev.playstreak.data.dao.** { *; }

# Keep model classes for Room and data handling
-keep class com.pseddev.playstreak.data.** { *; }

# Navigation component rules
-keep class androidx.navigation.** { *; }
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.lifecycle.ViewModel

# CalendarView library rules
-keep class com.kizitonwose.calendar.** { *; }
-dontwarn com.kizitonwose.calendar.**

# OpenCSV rules
-keep class com.opencsv.** { *; }
-dontwarn com.opencsv.**

# Google Drive API rules
-keep class com.google.api.** { *; }
-keep class com.google.http.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.http.**

# Gson rules (for JSON serialization)
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep application class
-keep class com.pseddev.playstreak.PlayStreakApplication { *; }

# Keep MainActivity for proper app launching
-keep class com.pseddev.playstreak.MainActivity { *; }

# Preserve enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# R8 missing classes warnings (generated automatically)
# These are Java enterprise classes not available on Android
-dontwarn javax.naming.InvalidNameException
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.ldap.LdapName
-dontwarn javax.naming.ldap.Rdn
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid