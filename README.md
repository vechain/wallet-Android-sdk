## Vechain Wallet Sdk

### Introduction

Vechain wallet SDK provides a series of functional interface can help the Android developers, for example: developers to quickly create the purse, the private key signature, call vechain block interface, data on the chain, and convenient call vechain connex.

#### Features:

##### Setting
- Set node url
- Get node url

##### Manage Wallet
- Create wallet
- Create wallet with mnemonic words
- Get checksum address
- Change Wallet password
- Verify mnemonic words
- Verify keystore

##### Sign
- Sign transaction
- Sign and send transaction

##### Support DApp development environment
- 100% support for the [connex.](https://github.com/vechain/connex/blob/master/docs/api.md/)
- Support for web3 features :getNodeUrl, getAccounts,sign,getBalance

## How to use


####  Gradle compiler environment（Android Studio）

Reference library

```groovy
implementation 'com.vechain:wallet:1.0.0'
```

The main module build.gradle configuration is as follows:

```groovy
minSdkVersion 21
```


Android SDK requires minimum system version API 19(Android 5.0).


SDK initialization
Inherit the android.app.Application class and implement the following methods:

```java
protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageManager.setLocale(base));
        SdkManager.initSdkManager(this);

        //Set it as a main_net environment
        WalletUtils.setNodeUrl(NodeUrl.MAIN_NODE);
 }
```
When app is test, it is set as a test net environment:
```java
WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);
```

Or if you have a corresponding node server, you can change it to your own node server:
```java
String customUrl = "https://www.yourCustomNodeUrl.com";
NodeUrl.CUSTOM_NODE.setUrl(customUrl);
WalletUtils.setNodeUrl(NodeUrl.CUSTOM_NODE);
```

Refer to the following API for more detailed usage





## API Reference：

[API Reference](https://vit.digonchain.com/vechain-mobile-apps/android-wallet-sdk/blob/master/API%20Reference.md) for VeChain Android app developers




## Packing confusion instructions

Add the following in the Proguard obfuscation configuration file:

```bash
-keep class com.vechain.**{*;}

#Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.* { *;}
-dontwarn com.google.gson.**


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
```





## License

Vechain Wallet SDK is licensed under the ```MIT LICENSE```, also included
in *LICENSE* file in the repository.

Copyright (c) 2019 VeChain <support@vechain.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.







