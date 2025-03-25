package com.sc.collectlibs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;


import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;


public class PhoneUtil {

    /**
     * 获取手机外部已使用存储空间
     *
     * @param context
     * @return 以M, G为单位的容量
     */
    public static long getUsedExternalMemorySize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockCountLong = statFs.getBlockCountLong();
        long usedLong = blockCountLong - availableBlocksLong;
        long blockSizeLong = statFs.getBlockSizeLong();
        return usedLong
                * blockSizeLong / 1024;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getPhoneNumber(Context context) {
        String tel = "";
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String[] permissions = new String[]{Manifest.permission.READ_SMS
                , Manifest.permission.READ_PHONE_STATE};
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            permissions[permissions.length - 1] = Manifest.permission.READ_PHONE_NUMBERS;
        }
        if (PermissionUtils.isGranted(permissions)) {
            tel = tm.getLine1Number();
        }
        if(TextUtils.isEmpty(tel)){
            return "unknown";
        }
        return tel;
    }

    /**
     * 获取可使用运行时内存
     *
     * @param context
     * @return
     */
    public static String getFreeMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
//        LogUtil.e("eeee","availMem"+Formatter.formatFileSize(context, mi.availMem));
//        LogUtil.e("eeee","totalMem"+Formatter.formatFileSize(context, mi.totalMem));
        // mi.availMem; 当前系统的可用内存
        // 将获取的内存大小规格化
        return Formatter.formatFileSize(context, mi.availMem);
    }

    /**
     * 基带版本
     */
    public static String getBaseBandVersion() {
        String version = "";
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method method =
                    clazz.getMethod("get", new Class[]{String.class, String.class});
            //            Object object = clazz.newInstance();
            Object result =
                    method.invoke(null, new Object[]{"gsm.version.baseband", "no message"});
            version = (String) result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }


    /**
     * 获取imei
     *
     * @return
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String imei = "unkown";
        try {
            imei = telephonyManager.getImei();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    /**
     * 获取手机外部存储空间
     *
     * @return 以M, G为单位的容量
     */
    public static long getExternalMemorySize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        return (blockCountLong * blockSizeLong) / 1024;
    }

    /**
     * 获取手机外部可用存储空间
     *
     * @return 以M, G为单位的容量
     */
    public static String getAvailableExternalMemorySize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(availableBlocksLong
                * blockSizeLong * 1.0 / 1000000000.0) + " GB";
    }

    public static String getTotalRAM(Context context) {
        try {
            ActivityManager manager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return Formatter.formatFileSize(context, info.totalMem);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getUsedRAM(Context context) {
        try {
            ActivityManager manager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return Formatter.formatFileSize(context, info.totalMem - info.availMem);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAvailRAM(Context context) {
        try {
            ActivityManager manager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return Formatter.formatFileSize(context, info.availMem);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTotalROM(Context context) {
        try {
            final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long totalCounts = statFs.getBlockCountLong();//总共的block数
            long availableCounts = statFs.getAvailableBlocksLong(); //获取可用的block数
            long size = statFs.getBlockSizeLong(); //每格所占的大小，一般是4KB==
            long availROMSize = availableCounts * size;//可用内部存储大小
            long totalROMSize = totalCounts * size; //内部存储总大小
            return Formatter.formatFileSize(context, totalROMSize);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getUsedROM(Context context) {
        try {
            final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long totalCounts = statFs.getBlockCountLong();//总共的block数
            long availableCounts = statFs.getAvailableBlocksLong(); //获取可用的block数
            long size = statFs.getBlockSizeLong(); //每格所占的大小，一般是4KB==
            long availROMSize = availableCounts * size;//可用内部存储大小
            long totalROMSize = totalCounts * size; //内部存储总大小
            return Formatter.formatFileSize(context, totalROMSize - availROMSize);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAvailROM(Context context) {
        try {
            final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long totalCounts = statFs.getBlockCountLong();//总共的block数
            long availableCounts = statFs.getAvailableBlocksLong(); //获取可用的block数
            long size = statFs.getBlockSizeLong(); //每格所占的大小，一般是4KB==
            long availROMSize = availableCounts * size;//可用内部存储大小
            long totalROMSize = totalCounts * size; //内部存储总大小
            return Formatter.formatFileSize(context, availROMSize);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取总运行时内存
     */
    public static long getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.e("eeee", num + "\t");
            }
            // 获得系统总内存，单位是KB
            int i = Integer.valueOf(arrayOfString[1]).intValue();
            //int值乘以1024转换为long类型
            initial_memory = new Long((long) i * 1024);
            localBufferedReader.close();
        } catch (IOException e) {
        }
        // Byte转换为KB或者MB，内存大小规格化
        return initial_memory / 1024;
    }

    /**
     * 获取可用运行时内存
     */
    public static long getUsedMemory(Context context) {
        ActivityManager am =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //        LogUtil.e("eeee","availMem"+Formatter.formatFileSize(context, mi.availMem));
        //        LogUtil.e("eeee","totalMem"+Formatter.formatFileSize(context, mi.totalMem));
        // mi.availMem; 当前系统的可用内存
        // 将获取的内存大小规格化
        return (mi.totalMem - mi.availMem) / 1024;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     */
    public static String getMacFromHardware() {
        try {
            if(NetworkInterface.getNetworkInterfaces()!=null){
                List<NetworkInterface> all =
                        Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "unknown";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取连接的WiFi名
     */
    public static String getConnectWifiSsid(Context context) {
        if (!PermissionUtils.isGranted(Manifest.permission.ACCESS_WIFI_STATE) || !PermissionUtils.isGranted(Manifest.permission.ACCESS_NETWORK_STATE)) {
            return "";
        }
        //获取ssid
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "").replace("<", "").replace(">", "");
        if (ssid.contains("unknown") || ssid.contains("Mobile")) {

            ssid = "unknown";
        }
        return ssid;
    }


    public static String getAndroidId(Context context) {
        String android_id = "";
        try {
            android_id = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        } catch (Exception e) {

        }

        return android_id;
    }


    /**
     * 获取手机ip
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {    // 当前使用2G/3G/4G网络
                try {
                    for (Enumeration<NetworkInterface> en =
                         NetworkInterface.getNetworkInterfaces();
                         en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr =
                             intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()
                                    && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {    // 当前使用无线网络
                WifiManager wifiManager =
                        (WifiManager) context.getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());    // 得到IPV4地址
                return ipAddress;
            }
        } else {
            // 当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     */
    static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 判断是否可以拨打电话
     */
    public static boolean canCallPhone(Context context) {
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        boolean canCallPhone = intent.resolveActivity(context.getPackageManager()) != null;
        return canCallPhone;
    }

    /**
     * 判断设备 是否使用代理上网
     */
    public static boolean isWifiProxy(Context context) {

        final boolean IS_ICS_OR_LATER =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    /**
     * 判断手机是否root过
     */
    public static boolean isRootSystem() {
        if (isRootSystem1() || isRootSystem2()) {
            // 可加其他判断 如是否装了权限管理的apk，大多数root 权限 申请需要app配合，也有不需要的，这个需要改su源码。因为管理su权限的app太多，无法列举所有的app，特别是国外的，暂时不做判断是否有root权限管理app
            //多数只要su可执行就是root成功了，但是成功后用户如果删掉了权限管理的app，就会造成第三方app无法申请root权限，此时是用户删root权限管理app造成的。
            //市场上常用的的权限管理app的包名   com.qihoo.permmgr  com.noshufou.android.su  eu.chainfire.supersu   com.kingroot.kinguser  com.kingouser.com  com.koushikdutta.superuser
            //com.dianxinos.superuser  com.lbe.security.shuame com.geohot.towelroot 。。。。。。
            return true;
        } else {
            return false;
        }
    }

    private static boolean isRootSystem1() {
        File f = null;
        final String kSuSearchPaths[] = {
                "/system/bin/", "/system/xbin/",
                "/system/sbin/", "/sbin/", "/vendor/bin/"
        };
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists() && f.canExecute()) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean isRootSystem2() {
        List<String> pros = Arrays.asList(System.getenv("PATH").split(":"));
        File f = null;
        try {
            for (int i = 0; i < pros.size(); i++) {
                f = new File(pros.get(i), "su");
                System.out.println("f.getAbsolutePath():" + f.getAbsolutePath());
                if (f != null && f.exists() && f.canExecute()) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 获取手机信号强度，需添加权限 android.permission.ACCESS_COARSE_LOCATION <br>
     * API要求不低于17 <br>
     *
     * @return 当前手机主卡信号强度, 单位 dBm（-1是默认值，表示获取失败）
     */
    public static int getMobileDbm(Context context) {
        int dbm = -1;
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList;
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cellInfoList = tm.getAllCellInfo();
            if (null != cellInfoList) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellSignalStrengthGsm cellSignalStrengthGsm =
                                ((CellInfoGsm) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthGsm.getDbm();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellSignalStrengthCdma cellSignalStrengthCdma =
                                ((CellInfoCdma) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthCdma.getDbm();
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if (Build.VERSION.SDK_INT
                                >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            CellSignalStrengthWcdma cellSignalStrengthWcdma =
                                    ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthWcdma.getDbm();
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte cellSignalStrengthLte =
                                ((CellInfoLte) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthLte.getDbm();
                    }
                }
            }
        }
        return dbm;
    }

    /**
     * 获取运营商名字
     */
    public static String getOperatorName(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         */
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        // getSimOperatorName就可以直接获取到运营商的名字
        String operatorName = telephonyManager.getSimOperatorName();
        if (operatorName == null) {
            operatorName = "";
        }
        return operatorName;
    }

    /**
     * 获取当前网络连接的类型
     *
     * @return 2G, 3G, 4G
     */
    public static String getNetworkState(Context context) {
        // 获取网络服务
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 为空则认为无网络
        if (null == connManager) {
            return "";
        }
        // 获取网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return "";
        }
        // 判断是否为WIFI
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED
                        || state == NetworkInfo.State.CONNECTING) {
                    return "NETWORK_WIFI";
                }
            }
        }
        // 若不是WIFI，则去判断是2G、3G、4G网
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        int networkType = telephonyManager.getNetworkType();
        //参考 TelephonyManager.getNetworkClass
        switch (networkType) {
            /*
             GPRS : 2G(2.5) General Packet Radia Service 114kbps
             EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
             UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
             CDMA : 2G 电信 Code Division Multiple Access 码分多址
             EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
             EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
             1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
             HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
             HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
             HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
             IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
             EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
             LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
             EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
             HSPAP : 3G HSPAP 比 HSDPA 快些
             */
            // 2G网络
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            // 3G网络
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "3G";
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "4G";
            //5G网络
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "";
        }
    }

    /**
     * 获取mcc
     */
    public static String getMcc(Context context) {
        /**
         * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志
         * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI = MCC + MNC + MSIN，
         * 其中MCC为移动国家号码，由3位数字组成唯一地识别移动客户所属的国家，我国为460；
         * MNC为网络id，由2位数字组成用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03
         *MSIN为移动客户识别码，采用等长11位数字构成。
         * */
        try {
            String mcc = "";
            if ((ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED)
            ) {
                return mcc;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return mcc;
            }
            TelephonyManager telManager =
                    (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            String imsi = telManager.getSubscriberId();
            Log.e("eeee", "getMcc imsi=" + imsi);
            if (!TextUtils.isEmpty(imsi) && imsi.length() > 3) {
                mcc = imsi.substring(0, 3);
                return mcc;
            }
            return mcc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取mnc
     */
    public static String getMnc(Context context) {
        String mnc = "";
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return mnc;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return mnc;
        }
        TelephonyManager telManager =
                (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();
        Log.e("eeee", "getMnc imsi=" + imsi);
        if (!TextUtils.isEmpty(imsi) && imsi.length() > 5) {
            mnc = imsi.substring(3, 5);
            return mnc;
        }
        return mnc;
    }

    /**
     * 获取语言
     */
    public static String getLanguage(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale != null) {
            return locale.getLanguage();
        } else {
            return "";
        }
    }

    /**
     * 获取时区
     */
    public static String getGmtTimeZone() {
        int timeZone =
                TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (3600 * 1000);
        if (timeZone >= 10) {
            return "GMT+" + timeZone + ":00";
        } else {
            return "GMT+0" + timeZone + ":00";
        }
    }

    /**
     * 获得系统屏幕亮度
     */
    public static int getSystemBrightness(Context context) {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    /**
     * 是否支持NFC
     */
    public static String hasNFC(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean hasNfc = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC);
        if (hasNfc) {
            return "Y";
        } else {
            NfcManager nfcManager =
                    (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                return "Y";
            } else {
                return "N";
            }
        }
    }

    /**
     * 相册遍历
     */
    public static String getGallery(Context mContext) {
        JSONArray jsonArray = new JSONArray();
        try {
            Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();

            // 只查询jpeg和png的图片
            Cursor mCursor = mContentResolver.query(imageUri, null,
                    MediaStore.Images.Media.MIME_TYPE + " in(?, ?)",
                    new String[]{"image/jpeg", "image/png"},
                    MediaStore.Images.Media.DATE_MODIFIED + " desc");

            int pathIndex = mCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);

            if (mCursor.moveToFirst()) {
                do {
                    // 获取图片的路径
                    String path = mCursor.getString(pathIndex);
                    try {
                        jsonArray.put(getInfo(path));
                    } catch (Exception e) {
                    }
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    /**
     * @param path 图片路径
     */
    private static JSONObject getInfo(String path) {
        JSONObject jsonObject = new JSONObject();
        try {
            File file = new File(path);
            ExifInterface exifInterface = new ExifInterface(path);
            String guangquan = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            String shijain = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String baoguangshijian =
                    exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            String jiaoju = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String chang = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            String kuan = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            String moshi = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String zhizaoshang = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO);
            String jiaodu = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            String baiph = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
            String altitude_ref = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_ALTITUDE_REF);
            String altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latitude_ref = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_LATITUDE_REF);
            String longitude_ref = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_LONGITUDE_REF);
            String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String timestamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            String processing_method = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_PROCESSING_METHOD);

            //转换经纬度格式
            double lat = score2dimensionality(latitude);
            double lon = score2dimensionality(longitude);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("光圈 = " + guangquan + "\n")
                    .append("时间 = " + shijain + "\n")
                    .append("曝光时长 = " + baoguangshijian + "\n")
                    .append("焦距 = " + jiaoju + "\n")
                    .append("长 = " + chang + "\n")
                    .append("宽 = " + kuan + "\n")
                    .append("型号 = " + moshi + "\n")
                    .append("制造商 = " + zhizaoshang + "\n")
                    .append("ISO = " + iso + "\n")
                    .append("角度 = " + jiaodu + "\n")
                    .append("白平衡 = " + baiph + "\n")
                    .append("海拔高度 = " + altitude_ref + "\n")
                    .append("GPS参考高度 = " + altitude + "\n")
                    .append("GPS时间戳 = " + timestamp + "\n")
                    .append("GPS定位类型 = " + processing_method + "\n")
                    .append("GPS参考经度 = " + latitude_ref + "\n")
                    .append("GPS参考纬度 = " + longitude_ref + "\n")
                    .append("GPS经度 = " + lat + "\n")
                    .append("GPS经度 = " + lon + "\n");
            try {
                jsonObject.put("date_time", shijain);
                jsonObject.put("image_length", file.length());
                jsonObject.put("image_width", kuan);
                jsonObject.put("longitude", lon);
                jsonObject.put("latitude", lat);
                jsonObject.put("name", file.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 将 112/1,58/1,390971/10000 格式的经纬度转换成 112.99434397362694格式
     *
     * @param string 度分秒
     * @return 度
     */
    private static double score2dimensionality(String string) {
        double dimensionality = 0.0;
        if (null == string) {
            return dimensionality;
        }

        //用 ，将数值分成3份
        String[] split = string.split(",");
        for (int i = 0; i < split.length; i++) {

            String[] s = split[i].split("/");
            //用112/1得到度分秒数值
            double v = Double.parseDouble(s[0]) / Double.parseDouble(s[1]);
            //将分秒分别除以60和3600得到度，并将度分秒相加
            dimensionality = dimensionality + v / Math.pow(60, i);
        }
        return dimensionality;
    }

    public static String getAppList(Context context) {


        JSONArray jsonArray = new JSONArray();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> aliveApps = getAppAliveList(context);
        for (PackageInfo packageInfo : packageInfos) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName",
                        packageInfo.applicationInfo.loadLabel(context.getPackageManager())
                                .toString());
                jsonObject.put("lastUpdateTime", packageInfo.lastUpdateTime + "");
                jsonObject.put("firstInstallTime", packageInfo.firstInstallTime + "");
                jsonObject.put("pkgName", packageInfo.packageName);
                jsonObject.put("versionName", packageInfo.versionName);
                jsonObject.put("isSystem",
                        ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                                == 0)
                                ? "0" : "1");
                jsonObject.put("isAppActive",
                        aliveApps.contains(packageInfo.packageName) ? "1" : "0");
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    /**
     * 获取已启动应用的包名列表
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private static List<String> getAppAliveList(
            Context context) {
        List<String> aliveApps = new ArrayList<>();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos =
                activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            aliveApps.add(processInfos.get(i).processName);
            Log.e("eeee", "processName=" + aliveApps.get(i));
        }
        return aliveApps;
    }
}
