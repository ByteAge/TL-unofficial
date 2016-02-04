-keep public class com.google.android.gms.* { public *; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**


-keepnames class chitchat.skin.*
-keep class android.**{*;}
-dontwarn android.**
-keep class com.**{*;}
-dontwarn com.**
-keep class org.**{*;}
-dontwarn org.**
-keep class net.**{*;}
-dontwarn net.**


-assumenosideeffects class android.util.Log {
  public static *** d(...);
}
-assumenosideeffects class chitchat.Log{
    public static *** d(...);
}




#-keep class io.realm.annotations.RealmModule
#-keep @io.realm.annotations.RealmModule class *
#-keep class io.realm.internal.Keep
#-keep @io.realm.internal.Keep class * { *; }
#-dontwarn javax.**
#-dontwarn io.realm.**