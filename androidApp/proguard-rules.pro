# Add project specific ProGuard rules here.

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Koin
-keep class org.koin.** { *; }
-keep class kotlin.Metadata { *; }

# Keep data classes for serialization
-keep class com.projectduoplay.** { *; }

# WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.projectduoplay.**$$serializer { *; }
-keepclassmembers class com.projectduoplay.** {
    *** Companion;
}
-keepclasseswithmembers class com.projectduoplay.** {
    kotlinx.serialization.KSerializer serializer(...);
}
