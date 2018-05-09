package cn.wwei.yzlsms;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wwei.yzlsms.app.MyApplication;
import cn.wwei.yzlsms.app.YZLService;
import cn.wwei.yzlsms.bean.BaseBean;
import cn.wwei.yzlsms.biz.GetRespBiz;
import cn.wwei.yzlsms.biz.HttpResponseInterface;
import cn.wwei.yzlsms.utils.Const;
import cn.wwei.yzlsms.utils.DataUtils;
import cn.wwei.yzlsms.utils.ExceptionUtils;
import cn.wwei.yzlsms.utils.LogCustom;
import cn.wwei.yzlsms.utils.MD5Util;
import cn.wwei.yzlsms.utils.SPUtils;


public class MainActivity extends Activity implements View.OnClickListener ,HttpResponseInterface {

    private static final String TAG = "YZL";
    private static final String MD5_KEY = "86bf2c32c569a134c8a5a5698b3ecd79";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initService();
        initView();
        initSetting();
        initData();
        /*try {
            Uri uri = Uri.parse("content://sms/inbox");
            ContentResolver provider = getContentResolver();
            Cursor cur = provider.query(uri, new String[]{"_id", "address", "body","date"}, "address like '%18923712372%'", null, "_id desc");
            String info = "";
            if (cur!=null){
                if (cur.moveToFirst()){
                    do{
                        String str = "";
                        for (int j=0;j<cur.getColumnCount();j++){
                            str += cur.getColumnName(j)+"="+cur.getString(j)+",";
                        }
                        LogCustom.i("YZL","========>"+str);
                    }while (cur.moveToNext());

//                    info = cur.getString(0);

                }
                cur.close();
            }


        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"test");
        }*/


        /*ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        String msg = "";
        while(cursor.moveToNext()) {
            int phoneColumn = cursor.getColumnIndex("address");
            int smsColumn = cursor.getColumnIndex("body");
            msg += cursor.getString(phoneColumn) + ":" + cursor.getString(smsColumn) + "\n";
        }
        cursor.close();*/
        /*
            Uri uri = Uri.parse("content://sms/inbox");

        */

    }

    ServiceConnection cion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyApplication.service = ((YZLService.YZLBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //设置service
    private void initService(){        ;
        try {
            Intent intent = new Intent(this,YZLService.class);
            intent.setAction(Const.SERVICE_START);
            intent.setPackage(getPackageName());
            this.bindService(intent, cion, Service.BIND_AUTO_CREATE);
        }catch (Exception e){
            ExceptionUtils.ExceptionToUM(e, this, "initService");
        }
    }

    private EditText et_phone,et_time,et_random;
    private TextView to_send;
    private CheckBox cb;
    //设置控件
    private void initView(){
        et_phone = (EditText)findViewById(R.id.et_phone);
        et_time = (EditText)findViewById(R.id.et_time);
        et_random = (EditText)findViewById(R.id.et_random);
        cb = (CheckBox)findViewById(R.id.cb);
        findViewById(R.id.tv_to_send).setOnClickListener(this);
    }

    //发送设置
    private void initSetting(){
        String phone = SPUtils.getString(this, Const.SP_PHONE_NUM, "");
        if (!TextUtils.isEmpty(phone)&&checkPhoneNum(phone)){
            et_phone.setText(phone);
        }
        int time = SPUtils.getInt(this, Const.SP_SPACE_TIME, 30);
        et_time.setText(time + "");
        int rn = SPUtils.getInt(this, Const.SP_RANDOM_NUM, 30);
        et_random.setText(rn + "");
        boolean test = SPUtils.getBoolean(this,Const.SP_TEST_FLAG,true);
        cb.setChecked(test);
    }

    //设置数据
    private void initData(){

    }

    private long toSendTime = 0;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_to_send:
                if ((System.currentTimeMillis() - toSendTime) > 2000) {
                    toSend();
                    toSendTime = System.currentTimeMillis();
                }
                break;
        }
    }

    /**
     * 直接调用短信接口发短信
     * @param phoneNumber
     * @param message
     */
    public void sendSMS(String phoneNumber,String message,PendingIntent sentPI,PendingIntent deliverPI){
        //获取短信管理器
        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
        //拆分短信内容（手机短信长度限制）
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliverPI);
        }
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (MyApplication.service!=null&&!MyApplication.service.isWorkEmpty()){
                showToast("还有未处理工作");
            }else {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void toSend(){
        String phone = et_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)){
            showToast("请输入手机号码");
            return;
        }
        if (!checkPhoneNum(phone)){
            showToast("手机号格式不正确");
            return;
        }
        MyApplication.PHONE_NUM = phone;
        SPUtils.saveString(this,Const.SP_PHONE_NUM,phone);
        String time = et_time.getText().toString().trim();
        if (!TextUtils.isEmpty(time)){
            MyApplication.SPACE_TIME = Integer.valueOf(time);
            SPUtils.saveInt(this,Const.SP_SPACE_TIME,MyApplication.SPACE_TIME);
        }else {
            MyApplication.SPACE_TIME = 30;
        }
        String rn = et_random.getText().toString().trim();
        if (!TextUtils.isEmpty(rn)){
            MyApplication.RANDOM_NUM = Integer.valueOf(rn);
            SPUtils.saveInt(this,Const.SP_RANDOM_NUM,MyApplication.RANDOM_NUM);
        }else {
            MyApplication.RANDOM_NUM = 30;
        }
        MyApplication.TestFlag = cb.isChecked();
        SPUtils.saveBoolean(this,Const.SP_TEST_FLAG,MyApplication.TestFlag);

        if (MyApplication.TestFlag){
            new GetRespBiz(this,Const.ResetTestData).postResponse();
        }else {
            toNext();
        }
    }

    private static final String REGEX_PHONE = "^[1][0-9]{10}$";
    private boolean checkPhoneNum(String num){
        Pattern p = Pattern.compile(REGEX_PHONE);
        Matcher m = p.matcher(num);
        return m.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cion!=null)
            unbindService(cion);
    }

    private void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    public Map getParamInfo(int tag, Object obj) {
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        if (tag == Const.ResetTestData){
            map.put("act","reset_test_data");
            map.put("send_mobile",MyApplication.PHONE_NUM);
            String time = DataUtils.getDateDetailForYzl(System.currentTimeMillis() + "");
            try {
                map.put("time", URLEncoder.encode(time, "UTF-8").toLowerCase());
            }catch (Exception e){
                e.printStackTrace();
            }
            map.put("v_code", MD5Util.MD5Encode("reset_test_data" + MyApplication.PHONE_NUM + time + Const.MD5_KEY, "UTF_8"));
        }
        return map;
    }

    @Override
    public byte[] getPostParams(int flag) {
        return new byte[0];
    }

    @Override
    public void toActivity(Object response, int flag, Object obj) {
        if (flag == Const.ResetTestData){
            BaseBean bean = (BaseBean)response;
            if (Const.RESULT.equals(bean.getError())){
                toNext();
            }else {
                showToast("测试数据重置失败");
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
        showToast("测试数据重置失败");
    }

    private void toNext(){
        startActivity(new Intent(this,SendMsgActivity.class));
    }
}
