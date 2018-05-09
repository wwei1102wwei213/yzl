package cn.wwei.yzlsms.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ta.util.db.TASQLiteDatabase;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.wwei.yzlsms.SendMsgActivity;
import cn.wwei.yzlsms.bean.BaseBean;
import cn.wwei.yzlsms.bean.MsgDetailBean;
import cn.wwei.yzlsms.bean.MsgYzlEntity;
import cn.wwei.yzlsms.bean.SendDataBean;
import cn.wwei.yzlsms.bean.SendTableBean;
import cn.wwei.yzlsms.biz.GetRespBiz;
import cn.wwei.yzlsms.biz.HttpResponseInterface;
import cn.wwei.yzlsms.biz.ReplySmsBiz;
import cn.wwei.yzlsms.db.DBHelper;
import cn.wwei.yzlsms.utils.Const;
import cn.wwei.yzlsms.utils.DataUtils;
import cn.wwei.yzlsms.utils.ExceptionUtils;
import cn.wwei.yzlsms.utils.LogCustom;
import cn.wwei.yzlsms.utils.MD5Util;


/**
 * Created by Administrator on 2017-04-29.
 *
 * @author wwei
 */
public class YZLService extends Service implements HttpResponseInterface {

    private static final String TAG = "YZL";
    //数据库列表
    private List<SendTableBean> beans = new ArrayList<>();
    //全部列表
    private List<MsgYzlEntity> data;
    //工作列表
    private List<MsgYzlEntity> work;
    //广播
    private MsgBroadcastReceiver receiver;
    //工作状态标识
    public boolean isWork = false;
    //列表页面显示标识
    private boolean isShow = false;
    //工作列表页面对象
    private SendMsgActivity activity = null;
    //列表数目
    private int num_all,num_finish,num_wait,num_fail;
    //失败列表
    private List<String> failList = new ArrayList<>();
    //下一个列表标识
    private int nextFlag;
    //PendingIntentCode
    private int PI_Code;
    public void setIsShow(boolean isShow) {
        this.isShow = isShow;
    }

    public boolean isWorkEmpty(){
        if (work==null||work.size()==0) return true;
        return false;
    }

    public boolean isStartNextFlag(){
        return startNextFlag;
    }

    public void setActivity(SendMsgActivity activity){
        this.activity = activity;
        if (activity!=null){
            if (data==null||data.size()==0){
                /*MsgYzlEntity entity = new MsgYzlEntity();
                entity.setOrder_id(3);
                new GetRespBiz(this,Const.PostFailedOrderIds,entity).postResponse();
                MsgYzlEntity entity1 = new MsgYzlEntity();
                entity1.setOrder_id(1);
                new GetRespBiz(this,Const.PostFailedOrderIds,entity1).postResponse();*/
                getData(Const.GetNewSms);
            }else {
                activity.update(data);
                isShow = true;
                setListNum();
            }
        }
    }

    public void reStart(){
        if (data!=null&&data.size()>0&&activity!=null){
            activity.update(data);
        }
    }

    public void setListNum(){
        if (activity!=null&&isShow){
            activity.setListNum(num_all,num_finish,num_wait,num_fail);
        }
    }

    public YZLService(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new YZLBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        data = new ArrayList<>();
        work = new ArrayList<>();
        receiver = new MsgBroadcastReceiver();
        getOldListForQuery();
        new ReplySmsBiz(this,beans).execute();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(999);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction(MyApplication.ACTION_SENT);
        filter.addAction(MyApplication.ACTION_RECEIVER);
        this.registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            if (Const.SERVICE_START.equals(intent.getAction())){
                LogCustom.i(TAG, "SERVICE_START");
            }else if(Const.SERVICE_LOAD_MORE.equals(intent.getAction())){

            }
        }
        return START_STICKY;
    }

    public void toStartFailList(){
        mHandler.post(failRunnable);
    }
    private static final int FAIL_MSG = 3;
    private Runnable failRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                long t = System.currentTimeMillis() - 3*1000*3600*24;
                List<MsgDetailBean> failMsgList = DBHelper.getInstance().queryReplyTable(MyApplication.application,"time>"+t+" and finish=0");
                LogCustom.i("YZL","failRunnable:"+failMsgList.toString());
                if (failMsgList!=null&&failMsgList.size()>0){
                    for (MsgDetailBean msgDetailBean:failMsgList){
                        LogCustom.i("YZL","failRunnable:"+msgDetailBean.toString());
                        Message msg = new Message();
                        msg.what = FAIL_MSG;
                        msg.obj = msgDetailBean;
                        mHandler.sendMessage(msg);
                    }
                }
                mHandler.postDelayed(failRunnable,1000*60*10);
            }catch (Exception e){
                ExceptionUtils.ExceptionSend(e,"failRunnable");
            }
        }
    };

    public void getData(int flag){
        new GetRespBiz(this,flag).postResponse();
    }

    public void getData(int flag, Object obj){
        new GetRespBiz(this,flag, obj).postResponse();
    }

    private boolean stop = false;

    public void toSendMsg(){
        try {
            if (stopFlag){
                Toast.makeText(this,"正在上传终止列表,请稍候",Toast.LENGTH_SHORT).show();
            } else if (stop&&runFlag){
                Toast.makeText(this,"延时执行尚未完成,请稍候",Toast.LENGTH_SHORT).show();
            }else if (stop&&isWork){
                Toast.makeText(this,"当前下标状态未返回，请稍候",Toast.LENGTH_SHORT).show();
            } else if (stop){
                stop = false;
                setSendBtnChange(0);
                Toast.makeText(this,"已切换为可发送状态",Toast.LENGTH_SHORT).show();
            } else {
                if (work==null||work.size()==0){
                    if (isWork){
                        if (!stop){
                            showStopDialog();
                        } else {
                            Toast.makeText(this,"当前下标状态未返回，请稍候",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(this,"没有数据需要处理或者已处理完毕",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (isWork){
                        if (!stop){
                            showStopDialog();
                        } else {
                            Toast.makeText(this,"当前下标状态未返回，请稍候",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        isWork = true;
                        setSendBtnChange(1);
                        index = -1;
                        toSend();
                    }
                }
            }

        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"toSendMsg");
            Toast.makeText(this,"E001",Toast.LENGTH_SHORT).show();
        }
    }

    private void showStopDialog(){
        if (activity!=null&&isShow){
            activity.showStopDialog();
        }
    }

    public void toStopList(){
        stop = true;
        setSendBtnChange(2);
        stopItem();
    }

    /**
     * flag
     *
     * @param flag
     */
    private void setSendBtnChange(int flag){
        if (activity!=null){
            activity.setSendBtnState(flag);
        }
    }

    private int index = -1;
    private List<MsgYzlEntity> list;
    public void toSend(){
        try {
            int workCount = work.size();
            list = new ArrayList<>();
            for (MsgYzlEntity entity:work){
                list.add(entity);
            }
            work = new ArrayList<>();
            num_wait = 0;
            setListNum();
            setWaitList(workCount);
            mHandler.post(sendRunnable);
        }catch (Exception e){
            Toast.makeText(this,"E002",Toast.LENGTH_SHORT).show();
        }
    }

    private void setWaitList(int count){
        try {
            for (int i=0;i<count;i++){
                data.get(data.size()-(i+1)).setSend(1);
            }
            update();
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"setWaitList");
        }
    }

    //0为完成状态,1为正在进行,2为暂停状态
    private boolean runFlag = false;
    Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                runFlag = false;
                index++;
                if (index>=list.size()){

                }else {
                    if (!stop&&index <= list.size()-1){
                        setMsgParams(list.get(index));
                        if (index != list.size()-1){
                            runFlag = true;
                            mHandler.postDelayed(sendRunnable,MyApplication.SPACE_TIME*1000 + getRandom());
                        }
                    }
                }
            }catch (Exception e){
                ExceptionUtils.ExceptionSend(e, "sendRunnable");
            }
        }
    };


    private int getRandom(){
        int temp = (int)(Math.random()*MyApplication.RANDOM_NUM);
        return temp*1000;
    }

    @Override
    public Map getParamInfo(int tag , Object obj) {
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        if (tag == Const.GetNewSms){
            map.put("act","get_new_sms");
            map.put("send_mobile",MyApplication.PHONE_NUM);
            if (MyApplication.TestFlag){
                map.put("is_testing","1");
            }else {
                map.put("is_testing","0");
            }
            String time = DataUtils.getDateDetailForYzl(System.currentTimeMillis() + "");
            try {
                map.put("time", URLEncoder.encode(time,"UTF-8").toLowerCase());
            }catch (Exception e){
                e.printStackTrace();
            }
            map.put("v_code", MD5Util.MD5Encode("get_new_sms" + MyApplication.PHONE_NUM + (MyApplication.TestFlag ? "1" : "0") + time + Const.MD5_KEY, "UTF_8"));

        } else if(tag == Const.PostReceipt){
            MsgYzlEntity entity = (MsgYzlEntity)obj;
            map.put("act","post_receipt");
            map.put("send_mobile",MyApplication.PHONE_NUM);
            map.put("sms_id",entity.getOrder_id()+"");
            map.put("sms_state",entity.getReceiver()==1?"received":"sent");
            String time = DataUtils.getDateDetailForYzl(System.currentTimeMillis() + "");
            try {
                map.put("time", URLEncoder.encode(time,"UTF-8").toLowerCase());
            }catch (Exception e){
                e.printStackTrace();
            }
            map.put("v_code", MD5Util.MD5Encode("post_receipt" + MyApplication.PHONE_NUM + entity.getOrder_id()
                    +(entity.getReceiver()==1?"received":"sent")+time + Const.MD5_KEY, "UTF_8"));
        } else if(tag == Const.PostReplySms){
            MsgDetailBean entity = (MsgDetailBean)obj;
            map.put("act","post_reply_sms");
            map.put("send_mobile",entity.getPhone());
            String phone_send = getSendPhoneNum(entity.getPhone());
            map.put("sms_mobile",phone_send);
            String time = DataUtils.getDateDetailForYzl(System.currentTimeMillis() + "");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = format.format(entity.getTime());
            try {
                map.put("send_time",URLEncoder.encode(dateStr,"UTF-8").toLowerCase());
                map.put("time", URLEncoder.encode(time,"UTF-8").toLowerCase());
                map.put("sms_content",URLEncoder.encode(entity.getBody(), "UTF-8").toLowerCase());
            }catch (Exception e){
                e.printStackTrace();
            }
            map.put("v_code", MD5Util.MD5Encode("post_reply_sms" + entity.getPhone() + phone_send
                    +URLEncoder.encode(entity.getBody()).toLowerCase()+ dateStr + time + Const.MD5_KEY, "UTF_8"));
        } else if(tag == Const.PostFailedOrderIds){
            MsgYzlEntity entity = (MsgYzlEntity)obj;
            map.put("act","post_failed_sms_ids");
            map.put("send_mobile",MyApplication.PHONE_NUM);
            String fail_ids;
            if (stopFlag){
                fail_ids = entity.getFail_ids();
            }else {
                fail_ids = entity.getOrder_id()+"";
            }
            map.put("failed_sms_ids",fail_ids);
            String time = DataUtils.getDateDetailForYzl(System.currentTimeMillis() + "");
            try {
                map.put("time", URLEncoder.encode(time,"UTF-8").toLowerCase());
            }catch (Exception e){
                e.printStackTrace();
            }
            map.put("v_code", MD5Util.MD5Encode("post_failed_sms_ids" + MyApplication.PHONE_NUM + fail_ids
                    +time + Const.MD5_KEY, "UTF_8"));
        }
        return map;
    }

    private String getSendPhoneNum(String phone){
        try {
            if (!TextUtils.isEmpty(phone)&&beans!=null&&beans.size()>0){
                for (int i=beans.size()-1;i>=0;i--){
                    if (phone.equals(beans.get(i).getPhone_user())){
                        return beans.get(i).getPhone_send();
                    }
                }
            }
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"getSendPhoneNum");
        }
        return MyApplication.PHONE_NUM;
    }


    private String getSendPhoneNum(String phone, boolean tag){
        try {
            if (!TextUtils.isEmpty(phone)&&beans!=null&&beans.size()>0){
                for (int i=beans.size()-1;i>=0;i--){
                    if (phone.contains(beans.get(i).getPhone_user())){
                        return beans.get(i).getPhone_send();
                    }
                }
            }
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"getSendPhoneNum");
        }
        return null;
    }

    @Override
    public byte[] getPostParams(int flag) {
        return new byte[0];
    }

    @Override
    public void toActivity(Object response, int flag, Object obj) {
        if (flag == Const.GetNewSms){
            SendDataBean bean = (SendDataBean)response;
            if (Const.RESULT.equals(bean.getError())){
                update(bean.getSms_list());
                if (startNextFlag){
                    changeNextUI(false);
                    startNextFlag = false;
                    toSendMsg();
                }
            }else {
                if (startNextFlag){
//                    changeNextUI(false);
//                    startNextFlag = false;
                    int hour = new Date(System.currentTimeMillis()).getHours();
                    if (MyApplication.TestFlag||(!MyApplication.TestFlag&&hour>9&&hour<21)){
                        toStartNextList();
                    }
                }
                update(null);
            }
        }else if (flag == Const.PostReceipt){

        }else if (flag == Const.PostReplySms){
            BaseBean bean = (BaseBean)response;
            if (Const.RESULT.equals(bean.getError())||"1".equals(bean.getError())){
                MsgDetailBean msgDetailBean = (MsgDetailBean)obj;
                msgDetailBean.setFinish(1);
                DBHelper.getInstance().insertReplyTableItem(MyApplication.application,msgDetailBean);
            }
        }else if (flag == Const.PostFailedOrderIds){
            BaseBean bean = (BaseBean)response;
            if (Const.RESULT.equals(bean.getError())||"1".equals(bean.getError())){
                if (stopFlag){
                    stopFlag = false;
                }
            }
        }
    }

    @Override
    public void showLoading(int flag) {

    }

    @Override
    public void hideLoading(int flag) {

    }

    @Override
    public void showError(int flag,Object obj) {

        if (flag == Const.GetNewSms){
            if (startNextFlag){
                int hour = new Date(System.currentTimeMillis()).getHours();
                if (MyApplication.TestFlag||(!MyApplication.TestFlag&&hour>9&&hour<21)){
                    toStartNextList();
                }else {
                    changeNextUI(false);
                    startNextFlag = false;
                }

            }
            update(null);
//            Toast.makeText(this,"E0031",Toast.LENGTH_SHORT).show();
        }else if (flag == Const.PostReplySms){
            DBHelper.getInstance().insertReplyTableItem(MyApplication.application,(MsgDetailBean)obj);
            Toast.makeText(this,"E0032",Toast.LENGTH_SHORT).show();
        }
    }

    //flag true:显示 false:隐藏
    private void changeNextUI(boolean flag){
        if (activity!=null){
            if (flag){
                activity.showNextProgressDialog();
            }else {
                activity.cancelNextProgressDialig();
            }
        }
    }

    private boolean start = false;
    private void update(List<MsgYzlEntity> list){
        if (list==null||list.size()==0){

        }else {
            if (nextFlag==0){
                nextFlag = 1;
            }else {
                for (int i=0;i<list.size();i++){
                    list.get(i).setNext(1);
                }
                nextFlag = 0;
            }
            insertSendTableItems(MyApplication.application, list);
            if (data==null) data = new ArrayList<>();
            if (work==null) work = new ArrayList<>();
            data.addAll(list);
            work.addAll(list);
            num_all += list.size();
            num_wait += list.size();
            update();
        }
        if (start){
            if (activity!=null) activity.loadingFinish();
        }else {
            start = true;
        }
        /*if (startNextFlag) {
            activity.loadingFinish();
        }*/
    }

    private void update(){
        if (activity!=null&&isShow) {
            activity.update(data);
            setListNum();
        }
    }

    /**
     * 刷新列表状态
     * @param entity 列表项
     * @param flag  标识 0为发送标识 1为接收标识
     * @param status  状态
     */
    private void updateItem(MsgYzlEntity entity, int flag, int status, int pos){
//        int pos = getItemPosition(entity);
        if (pos==-1) return;
        if (flag==0){
            if (data!=null) {
                data.get(pos).setSend(status);
                if (!TextUtils.isEmpty(entity.getSend_time())){
                    data.get(pos).setSend_time(entity.getSend_time());
                }
            }
        }else if(flag==1){
            if (data!=null) {
                data.get(pos).setReceiver(status);
                if (!TextUtils.isEmpty(entity.getReceive_time())){
                    data.get(pos).setReceive_time(entity.getReceive_time());
                }
            }
        }
        update();
    }

    private boolean stopFlag = false;
    /**
     * 终止列表状态
     */
    private void stopItem(){
//        int pos = getItemPosition(entity);
        if (index<list.size()-1){
            stopFlag = true;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(System.currentTimeMillis());
            String fial_ids = "";
            for (int i=index+1;i<list.size();i++){
                MsgYzlEntity entity = list.get(i);
                int pos = getItemPosition(entity);
                if (pos!=-1&&data!=null){
                    data.get(pos).setSend(4);
                    data.get(pos).setReceiver(3);
                    data.get(pos).setSend_time(time);
                    data.get(pos).setReceive_time(time);
                }
                fial_ids += entity.getOrder_id()+"";
                if (i!=list.size()-1){
                    fial_ids += ",";
                }
            }
            num_fail  = num_fail + (list.size()-1-index);
            setListNum();
            update();
            MsgYzlEntity failEntity  = new MsgYzlEntity();
            failEntity.setFail_ids(fial_ids);
            new GetRespBiz(this,Const.PostFailedOrderIds,failEntity).postResponse();
        }
        isWork = false;
        /*if (pos==-1) return;
        if (flag==0){
            if (data!=null) {
                data.get(pos).setSend(status);
                if (!TextUtils.isEmpty(entity.getSend_time())){
                    data.get(pos).setSend_time(entity.getSend_time());
                }
            }
        }else if(flag==1){
            if (data!=null) {
                data.get(pos).setReceiver(status);
                if (!TextUtils.isEmpty(entity.getReceive_time())){
                    data.get(pos).setReceive_time(entity.getReceive_time());
                }
            }
        }*/

    }

    private int getItemPosition(MsgYzlEntity entity){
        try {
            LogCustom.e("EXCO","=======>"+entity.toString()+"/"+data.toString());
            if (data!=null&&data.size()>0){
                for (int i=data.size()-1;i>=0;i--){
                    LogCustom.e("EXCO","i="+i+"=======>"+entity.toString()+"/"+data.get(i).toString());
                    if (entity.getOrder_id()==data.get(i).getOrder_id()) return i;
                }
            }
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"getItemPosition");
        }

        return -1;
    }

    /***
     * 提供activity获取service对象的方法
     *
     */
    public class YZLBinder extends Binder {
        public YZLService getService(){
            return YZLService.this;
        }
    }

    @Override
    public void onDestroy() {
        if (receiver!=null)
            this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    private final MyHandler mHandler = new MyHandler(this);
    private static final int IS_WORK_CHANGED = 1;
    /**
     *
     * Handler静态内部类
     *
     */
    private static class MyHandler extends Handler{
        private final WeakReference<YZLService> mService;
        public MyHandler(YZLService service){
            mService = new WeakReference<>(service);
        }
        private YZLService service;
        @Override
        public void handleMessage(Message msg) {
            service = mService.get();
            if (msg.what==IS_WORK_CHANGED){
                service.setSendBtnChange(service.stop?2:0);
                service.toStartNextList();
            }else if (msg.what == FAIL_MSG){
                service.toFailMsgItem((MsgDetailBean) msg.obj);
            }
        }
    }

    private boolean startNextFlag = false;
    private boolean nextTimeFlag = false;

    public void setNextTimeFlag(boolean nextTimeFlag) {
        this.nextTimeFlag = nextTimeFlag;
    }

    private void toStartNextList(){
        changeNextUI(true);
        startNextFlag = true;
        nextTimeFlag = true;
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    Thread.sleep(60000);
                }catch (Exception e){

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void sid) {
                if (nextTimeFlag){
                    LogCustom.i("YZL","自动获取下一个列表");
                    getData(Const.GetNewSms);
                }else {
                    startNextFlag = false;
                }
            }
        }.execute();
    }

    private void toFailMsgItem(MsgDetailBean msgDetailBean){
        try {
            new GetRespBiz(this,Const.PostReplySms,msgDetailBean).postResponse();
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"toFailMsgItem");
        }

    }

    private void getOldListForQuery(){
        try {
            beans = DBHelper.getInstance().querySendTable((MyApplication)getApplication());
            if (beans==null) beans = new ArrayList<>();
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"getOldListForQuery");
        }
    }

    public void insertSendTableItems(MyApplication application,List<MsgYzlEntity> list){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            for (MsgYzlEntity entity:list){
                SendTableBean bean = new SendTableBean();
                bean.setYzl_id(entity.getOrder_id());
                bean.setPhone_send(MyApplication.PHONE_NUM);
                bean.setPhone_user(entity.getSms_mobile());
                bean.setTime(System.currentTimeMillis());
                beans.add(bean);
                tasqLiteDatabase.insert(bean);
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"insertSendTableItems");
        }
    }

    private void setMsgParams(MsgYzlEntity entity){
        try {
            LogCustom.i("EXCO",""+entity);
            int pos = getItemPosition(entity);
            //处理返回的发送状态
            Intent sentIntent = new Intent(MyApplication.ACTION_SENT);
//            sentIntent.putExtra(Const.FLAG_SEND_FINISH, entity);
            sentIntent.putExtra(MyApplication.PENDING_SENT_INTENT,pos);
            PI_Code++;
            PendingIntent sentPI = PendingIntent.getBroadcast(this, PI_Code, sentIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            //处理返回的接收状态
            Intent deliverIntent = new Intent(MyApplication.ACTION_RECEIVER);
            deliverIntent.putExtra(MyApplication.PENDING_RECEIVER_INTENT, pos);
            PI_Code++;
            PendingIntent deliverPI = PendingIntent.getBroadcast(this, PI_Code, deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            sendSMS(entity.getSms_mobile(),entity.getSms_content(),sentPI,deliverPI);
//            sendSMS("13723701704",entity.getSms_content(),sentPI,deliverPI);
        }catch (Exception e){
            Toast.makeText(this,"E004",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 直接调用短信接口发短信
     * @param phoneNumber
     * @param message
     */
    public void sendSMS(String phoneNumber,String message,PendingIntent sentPI,PendingIntent deliverPI){
        try {

            //获取短信管理器
            SmsManager sms = SmsManager.getDefault();
            //拆分短信内容（手机短信长度限制）
            if (message.length() > 70) {
                ArrayList<String> msgs = sms.divideMessage(message);
                ArrayList<PendingIntent> sentIntents =  new ArrayList<PendingIntent>();
                for(int i = 0;i<msgs.size();i++){
                    sentIntents.add(sentPI);
                }
                ArrayList<PendingIntent> deliverIntents =  new ArrayList<PendingIntent>();
                for(int i = 0;i<msgs.size();i++){
                    deliverIntents.add(deliverPI);
                }
                sms.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, deliverIntents);
            } else {
                sms.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
            }
        }catch (Exception e){
            StackTraceElement[] stack = e.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<stack.length;i++){
                builder.append(stack[i].toString()+"\n");
            }
            Toast.makeText(this,"E005"+builder.toString(),Toast.LENGTH_LONG).show();
            LogCustom.i("EXCO",builder.toString());
        }
    }

    class MsgBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent!=null){
                int resultCode = getResultCode();
                if (Const.SERVICE_START.equals(intent.getAction())){
                    LogCustom.i(TAG, "SERVICE_START");
                }else if (MyApplication.ACTION_SENT.equals(intent.getAction())){
                    // TODO: 2017/6/19
                    int pos = intent.getIntExtra(MyApplication.PENDING_SENT_INTENT,-1);
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            try {
                                Bundle bundle1= intent.getExtras();
                                if(bundle1==null){
                                }else{
                                    Log.i(TAG, "短信发送成功pos:" + pos);
                                    if (pos!=-1&&data.get(pos).getSend()!=2){
                                        data.get(pos).setSend(2);
                                        data.get(pos).setSend_time(DataUtils.getDateDetailForYzl(System.currentTimeMillis()+""));
                                        updateItem(data.get(pos), 0, 2,pos);
                                        num_finish++;
                                        setListNum();
                                        new GetRespBiz(YZLService.this,Const.PostReceipt,data.get(pos)).postResponse();
                                        Log.i(TAG, "短信发送成功:" + data.get(pos).toString());
                                        if (index == list.size()-1){
                                            isWork = false;
                                            mHandler.sendEmptyMessage(IS_WORK_CHANGED);
                                        }
                                    }

                                }
                            }catch (Exception e){
                                ExceptionUtils.ExceptionSend(e,"短信发送成功 RESULT_OK");
                            }

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            if (pos!=-1&&!failList.contains(data.get(pos).getOrder_id()+"")){
                                num_fail++;
                                failList.add(data.get(pos).getOrder_id()+"");
                                data.get(pos).setSend(3);
                                updateItem(data.get(pos), 0, 3,pos);
                                if (index == list.size()-1){
                                    isWork = false;
                                    mHandler.sendEmptyMessage(IS_WORK_CHANGED);
                                }
                                new GetRespBiz(YZLService.this,Const.PostFailedOrderIds,data.get(pos)).postResponse();
                            }
                            break;
                    }
                }else if(MyApplication.ACTION_RECEIVER.equals(intent.getAction())){
                    int pos = intent.getIntExtra(MyApplication.PENDING_RECEIVER_INTENT,-1);

                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            try {
                                Bundle bundle1= intent.getExtras();
                                if(bundle1==null){
                                }else{

                                    Log.i(TAG, "短信接收成功:" + pos);
                                    if (pos!=-1&&data.get(pos).getReceiver()!=1){
                                        data.get(pos).setReceiver(1);
                                        data.get(pos).setReceive_time(DataUtils.getDateDetailForYzl(System.currentTimeMillis()+""));
                                        updateItem(data.get(pos), 1, 1,pos);
                                        new GetRespBiz(YZLService.this,Const.PostReceipt,data.get(pos)).postResponse();
                                        Log.i(TAG, "短信接收成功:" + data.get(pos).toString());
                                    }
                                    /*if (index == list.size()-1){
                                        isWork = false;
                                        mHandler.sendEmptyMessage(IS_WORK_CHANGED);
                                    }*/
                                }
                            }catch (Exception e){
                                ExceptionUtils.ExceptionSend(e,"短信接收成功 RESULT_OK");
                            }

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.i(TAG, "短信接收失败");
                            if (pos!=-1){
                                data.get(pos).setReceiver(2);
                                updateItem(data.get(pos), 1, 2,pos);
                            }

                            /*if (index == list.size()-1){
                                isWork = false;
                                mHandler.sendEmptyMessage(IS_WORK_CHANGED);
                            }*/
                            break;
                    }
                }else {
                    try {
                        Bundle bundle = intent.getExtras();
                        Object[] objects = (Object[]) bundle.get("pdus");
                        final MsgDetailBean bean = new MsgDetailBean();
                        String bodyAll = "";
                        for(Object obj : objects){
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
                            String body = smsMessage.getDisplayMessageBody();
                            String address = smsMessage.getDisplayOriginatingAddress();
                            long date = smsMessage.getTimestampMillis();
                            LogCustom.i(TAG, address + " 于  " + date + "给你发了以下内容: " + body);
                            bean.setPhone(address);
                            if (!TextUtils.isEmpty(body)) bodyAll += body.trim();
                            bean.setTime(date);
                        }
                        bean.setBody(bodyAll);
                        new AsyncTask<Void,Void,String>(){
                            @Override
                            protected String doInBackground(Void... voids) {
                                try {
                                    Thread.sleep(2000);
                                }catch (Exception e){

                                }
                                String f = getSendPhoneNum(bean.getPhone(),false);
                                return f;
                            }

                            @Override
                            protected void onPostExecute(String f) {
                                LogCustom.i("YZL","F:"+f);
                                if (!TextUtils.isEmpty(f)){
                                    new AsyncTask<Void,Void,String>(){
                                        @Override
                                        protected String doInBackground(Void... voids) {
                                            String info = "";
                                            try {
                                                Uri uri = Uri.parse("content://sms/inbox");
                                                ContentResolver provider = YZLService.this.getContentResolver();
                                                Cursor cur = provider.query(uri, new String[]{"_id", "address", "body"}, "address like '%"
                                                        + bean.getPhone() + "%'", null, "_id desc");

                                                if (cur!=null){
                                                    if (cur.moveToFirst()){
                                                        info = cur.getString(0);
                                                    }
                                                }
                                                if (!TextUtils.isEmpty(info)){
                                                    ContentValues values = new ContentValues();
                                                    values.put("read", "1");
                                                    provider.update(uri, values, "_id=" + info, new String[]{});
                                                    LogCustom.i("YZL", "新短信已标为已读"+"_id="+info);
                                                }
                                                if (cur!=null) cur.close();
                                            }catch (Exception e){
                                                ExceptionUtils.ExceptionSend(e, "sms test");
                                            }
                                            return info;
                                        }

                                        @Override
                                        protected void onPostExecute(String sid) {
                                            LogCustom.i("YZL","sid:"+sid);
                                            if (!TextUtils.isEmpty(sid)){
                                                bean.setSid(sid);
                                                new GetRespBiz(YZLService.this,Const.PostReplySms,bean).postResponse();
                                            }
                                        }
                                    }.execute();
                                }
                            }
                        }.execute();

                    }catch (Exception e){
                        ExceptionUtils.ExceptionSend(e,"Read Msg Error");
                        LogCustom.i(TAG, "Read Msg Error");
                    }
                }
            }
        }
    }




}
