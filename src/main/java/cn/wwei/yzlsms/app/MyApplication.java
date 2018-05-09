package cn.wwei.yzlsms.app;

import android.content.Context;
//import android.support.multidex.MultiDex;
//import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;

import com.ta.TAApplication;

import java.util.UUID;

import cn.wwei.yzlsms.db.DBHelper;
import cn.wwei.yzlsms.utils.Const;
import cn.wwei.yzlsms.utils.ExceptionUtils;


/**
 * Created by Administrator on 2017-04-29.
 *
 * @author wwei
 */
public class MyApplication extends TAApplication{

    //Application全局对象
    public static MyApplication application;
    //UUID
    public static String deviceId;
    //发送间隔
    public static int SPACE_TIME;
    //手机号码
    public static String PHONE_NUM;
    //随机数范围
    public static int RANDOM_NUM;
    //Service
    public static YZLService service;
    //test标识
    public static boolean TestFlag = true;

    public static String ACTION_SENT;
    public static String ACTION_RECEIVER;
    public static String PENDING_SENT_INTENT;
    public static String PENDING_RECEIVER_INTENT;


    @Override
    public void onCreate() {
        super.onCreate();
        //提升兼容性
//        MultiDex.install(this);
        application = this;
        long time = System.currentTimeMillis();
        ACTION_SENT = Const.ACTION_SENT_HEAD_INTENT + time;
        ACTION_RECEIVER = Const.ACTION_RECEIVER_HEAD_INTENT + time;
        PENDING_SENT_INTENT = Const.PENDING_TAG_SENT_HEAD_INTENT + time;
        PENDING_RECEIVER_INTENT = Const.PENDING_TAG_RECEIVER_HEAD_INTENT + time;
        deviceId = getDeviceID();
        initTable();
    }

    private String getDeviceID(){
        try {
            final TelephonyManager tm = (TelephonyManager) getBaseContext().
                    getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(),
                    ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
            return deviceUuid.toString();
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e, "getDeviceID");
            return "DeviceID_Error";
        }
    }

    private void initTable(){
//        DBHelper.getInstance().initDropSendTable(this);
        DBHelper.getInstance().initCreateSendTable(this);
        DBHelper.getInstance().initCreateReplyTable(this);
    }


}
