# CollectLibs

## 介绍

一个获取用户通讯录，App列表，短信列表，通话记录等信息的库

## 安装

在项目根目录的 build.gradle 添加仓库

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

然后在模块的 build.gradle 添加依赖

```groovy
dependencies {
    implementation 'com.github.404N:CollectLibs:1.2.3'
}
```

### 使用说明

#### 获取短信列表：

```kotlin
AppInfoUtil.getSmsList(Context context)
```

// 新版方法增加了2个参数sendType和status，用于获取短信类型，如收件箱、发件箱、草稿箱和短信发送状态等
```kotlin
AppInfoUtil.getSmsListType(Context context)
```

必要的权限:

```xml

<uses-permission android:name="android.permission.READ_SMS" />
```

#### 获取通话记录：

```kotlin
AppInfoUtil.getRecord(Context context)
```

必要的权限:

```xml

<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

#### 获取App列表：

```kotlin
AppInfoUtil.getAppList(Context context)
```

#### 获取通讯录：

```kotlin
AppInfoUtil.getContactString(Context context)
```

必要的权限:

```xmlxml
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

#### 获取手机信息：

```kotlin
//（旧版方法）运行在主线程的方法，可能会导致取不到gaid，若使用此方法，须在子线程，才能取到gaid
// needPosition为true时需要已经申请定位权限
AppInfoUtil.getDeviceInfo(
    Activity context,
    int authid,
    boolean needPosition,
)

//运行在子线程的方法，可以取到gaid,但是需要传入一个回调接口
// needPosition为true时需要已经申请定位权限
AppInfoUtil.getDeviceInfo(
    Activity context,
    GetDeviceInfo getDeviceInfo,
    int authid,
    boolean needPosition,
) 
```
新版方法使用示例
Java
```java
AppInfoUtil.getDeviceInfo(
    this,
    new GetDeviceInfo() {
        @Override
        public void getDeviceInfo(String s) {
            Log.d("MainActivity", "getDeviceInfo: " + s);
        }
    },
    1,
    true
);
```
Kotlin
```kotlin
AppInfoUtil.getDeviceInfo(
    this,
    object : GetDeviceInfo {
        override fun getDeviceInfo(s: String) {
            Log.d("MainActivity", "getDeviceInfo: $s")
        }
    },
    1,
    true
)
```

可选的权限:

```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

以上方法均返回json格式的字符串

使用时请确保获取了相关权限，若没有相关权限，对应方法返回值均为空字符串.
