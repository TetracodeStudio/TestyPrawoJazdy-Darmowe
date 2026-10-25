# -------------------------------------------------------------
# General Kotlin & Coroutines Keep Rules
# -------------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable

-keepclassmembers class * {
    @kotlin.jvm.Transient *;
}

# -------------------------------------------------------------
# Kotlinx Serialization Keep Rules
# -------------------------------------------------------------
-keepattributes *Annotation*, ElementValueAttribute
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** $serializer;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep,allowobfuscation,allowshrinking class * implements kotlinx.serialization.KSerializer
-keepclassmembers class kotlinx.serialization.json.JsonObject { *; }

# -------------------------------------------------------------
# Application Models & Data Classes (com.testy)
# -------------------------------------------------------------
-keep class com.testy.model.** { *; }
-keep class com.testy.data.** { *; }
-keep class com.testy.parser.** { *; }
-keep class com.testy.domain.** { *; }

# Keep App Entry Points & Platform Providers
-keep class com.testy.MainActivity { *; }
-keep class com.testy.Main* { *; }

# -------------------------------------------------------------
# Jetpack Compose & Compose Multiplatform Keep Rules
# -------------------------------------------------------------
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# -------------------------------------------------------------
# VLCJ Keep Rules (Compose Desktop Video Player)
# -------------------------------------------------------------
-keep class uk.co.caprica.vlcj.** { *; }
-dontwarn uk.co.caprica.vlcj.**
-dontwarn com.sun.jna.**
-keep class com.sun.jna.** { *; }

# -------------------------------------------------------------
# Okio Keep Rules
# -------------------------------------------------------------
-dontwarn okio.**
-keep class okio.** { *; }
