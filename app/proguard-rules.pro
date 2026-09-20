# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities
-keep class com.techapp.data.model.** { *; }

# Keep BCrypt
-keep class org.mindrot.jbcrypt.** { *; }

# Keep ViewBinding
-keep class com.techapp.databinding.** { *; }
