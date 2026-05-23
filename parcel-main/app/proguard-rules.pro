# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.chenran.parcel.**$$serializer { *; }
-keepclassmembers class com.chenran.parcel.** {
    *** Companion;
}
-keepclasseswithmembers class com.chenran.parcel.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Compose ---
-dontwarn androidx.compose.**

# --- Coil ---
-keep class coil.** { *; }

# --- WorkManager ---
-keep class * extends androidx.work.Worker

# --- Keep Parcelable ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# --- Keep data classes used with serialization ---
-keep class com.chenran.parcel.model.** { *; }
-keep class com.chenran.parcel.util.** { *; }
-keep class com.chenran.parcel.viewmodel.** { *; }
-keep class com.chenran.parcel.ui.** { *; }
-keep class com.chenran.parcel.widget.** { *; }

# --- Service & Receiver ---
-keep class com.chenran.parcel.service.** { *; }
-keep class com.chenran.parcel.receiver.** { *; }

# --- MainActivity ---
-keep class com.chenran.parcel.MainActivity { *; }

# --- AndroidX ---
-keep class androidx.lifecycle.** { *; }
