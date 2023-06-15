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
    implementation 'com.github.404N:CollectLibs:1.0.4'
}
```

### 使用说明

#### 获取短信列表：
```kotlin
AppInfoUtil.getSmsList(Context context)
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
AppInfoUtil.getDeviceInfo(Activity context, int authid)
```

可选的权限:
```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

以上方法均返回json格式的字符串


使用时请确保获取了相关权限，若没有相关权限，对应方法返回值均为空字符串.
