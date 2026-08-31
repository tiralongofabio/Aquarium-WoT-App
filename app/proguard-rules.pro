# Retrofit
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}


# Moshi (Preserva i modelli dati e gli adapter generati da KSP)
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class *
-keep class * extends com.squareup.moshi.JsonAdapter {
    public <init>(...);
}


# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**


# Coroutines & Hilt (Prevenzione falsi allarmi in build)
-dontwarn kotlinx.coroutines.**
-dontwarn dagger.**
