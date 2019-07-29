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

 -keep class com.vechain.**{*;}
 -keepattributes *Annotation*

 #Gson
 -keepattributes Signature
 -keepattributes *Annotation*
 -keep class sun.misc.Unsafe { *; }
 -keep class com.google.gson.stream.** { *; }
 -keep class com.google.gson.examples.android.model.** { *; }
 -keep class com.google.gson.* { *;}
 -dontwarn com.google.gson.**



 #rxbinding
 -keep class com.jakewharton.**

 -keep class com.squareup.**
 -keep class com.tbruyelle.**
 -keep class com.trello.**
 -keep class io.reactivex.**
 -keep class org.hamcrest.**
 -keep class org.reactivestreams.**


 #okhttp
 -dontwarn okhttp3.**
 -dontwarn okio.**
 -dontwarn javax.annotation.**
 -dontwarn org.conscrypt.**
 #A resource is loaded with a relative path so the package of this class must be preserved.
 -keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase


 #Retrofit
 #Retain generic type information for use by reflection by converters and adapters.
 -keepattributes Signature
 #Retain service method parameters.
 -keepclassmembernames,allowobfuscation interface * {
     @retrofit2.http.* <methods>;
 }
 #Ignore annotation used for build tooling.
 -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement


 #Retrofit
 -dontwarn retrofit2.**
 -keep class retrofit2.** { *; }
 #-keepattributes Signature-keepattributes Exceptions
 #RxJava RxAndroid
 -dontwarn sun.misc.**
 -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
     long producerIndex;
     long consumerIndex;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
     rx.internal.util.atomic.LinkedQueueNode producerNode;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
     rx.internal.util.atomic.LinkedQueueNode consumerNode;
 }


 #-------------------------5.基本不用动区域--------------------------
 #指定代码的压缩级别
 -optimizationpasses 5

 #包明不混合大小写
 -dontusemixedcaseclassnames

 #不去忽略非公共的库类
 -dontskipnonpubliclibraryclasses
 -dontskipnonpubliclibraryclassmembers

 #混淆时是否记录日志
 -verbose

 #优化  不优化输入的类文件
 -dontoptimize

 #预校验
 -dontpreverify

 # 保留sdk系统自带的一些内容 【例如：-keepattributes *Annotation* 会保留Activity的被@override注释的onCreate、onDestroy方法等】
 -keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

 # 记录生成的日志数据,gradle build时在本项根目录输出
 # apk 包内所有 class 的内部结构
 -dump proguard/class_files.txt
 # 未混淆的类和成员
 -printseeds proguard/seeds.txt
 # 列出从 apk 中删除的代码
 -printusage proguard/unused.txt
 # 混淆前后的映射
 -printmapping proguard/mapping.txt


 # 避免混淆泛型
 -keepattributes Signature
 # 抛出异常时保留代码行号,保持源文件以及行号
 -keepattributes SourceFile,LineNumberTable

 #-----------------------------6.默认保留区-----------------------
 # 保持 native 方法不被混淆
 -keepclasseswithmembernames class * {
     native <methods>;
 }

 -keepclassmembers public class * extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
  public void set*(***);
 }

 #保持 Serializable 不被混淆
 -keepclassmembers class * implements java.io.Serializable {
     static final long serialVersionUID;
     private static final java.io.ObjectStreamField[] serialPersistentFields;
     !static !transient <fields>;
     !private <fields>;
     !private <methods>;
     private void writeObject(java.io.ObjectOutputStream);
     private void readObject(java.io.ObjectInputStream);
     java.lang.Object writeReplace();
     java.lang.Object readResolve();
 }

 # 保持自定义控件类不被混淆
 -keepclasseswithmembers class * {
     public <init>(android.content.Context,android.util.AttributeSet);
 }
 # 保持自定义控件类不被混淆
 -keepclasseswithmembers class * {
     public <init>(android.content.Context,android.util.AttributeSet,int);
 }
 # 保持自定义控件类不被混淆
 -keepclassmembers class * extends android.app.Activity {
     public void *(android.view.View);
 }

 # 保持枚举 enum 类不被混淆
 -keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }

 # 保持 Parcelable 不被混淆
 -keep class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator *;
 }

 # 不混淆R文件中的所有静态字段，我们都知道R文件是通过字段来记录每个资源的id的，字段名要是被混淆了，id也就找不着了。
 -keepclassmembers class **.R$* {
     public static <fields>;
 }

 #如果引用了v4或者v7包
 -dontwarn android.support.**

 # 保持哪些类不被混淆
 -keep public class * extends android.app.Appliction
 -keep public class * extends android.app.Activity
 -keep public class * extends android.app.Fragment
 -keep public class * extends android.app.Service
 -keep public class * extends android.content.BroadcastReceiver
 -keep public class * extends android.content.ContentProvider
 -keep public class * extends android.preference.Preference


 # ============忽略警告，否则打包可能会不成功=============
 -ignorewarnings