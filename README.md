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
    implementation 'com.github.404N:CollectLibs:1.0.0'
}
```

### 使用说明

获取短信列表：
AppInfoUtils.getSmsList(Context context)

获取通话记录：
AppInfoUtils.getCallLogList(Context context)

获取App列表：
AppInfoUtils.getAppList(Context context)

获取通讯录：
AppInfoUtils.getContactList(Context context)

获取手机信息：
AppInfoUtils.getDeviceInfo(Context context)