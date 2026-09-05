-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keep class br.unoesc.linhaviva.data.remote.dto.** { *; }
-keep class br.unoesc.linhaviva.data.local.entity.** { *; }

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
