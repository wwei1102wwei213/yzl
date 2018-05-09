package cn.wwei.yzlsms.biz;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.Map;

import cn.wwei.yzlsms.utils.ExceptionUtils;
import cn.wwei.yzlsms.utils.LogCustom;


/**
 * Created by Administrator on 2017-05-04.
 */
public class WriteSmsReadStateBiz extends AsyncTask<Void,Void,Void> implements HttpResponseInterface{

    private Context context;
    private String address;

    public WriteSmsReadStateBiz(Context context,String address){
        this.context = context;
        if (address.startsWith("+86")) address = address.substring(3,address.length());
        this.address = address;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            ContentResolver provider = context.getContentResolver();
            Cursor cur = provider.query(uri, new String[]{"_id", "address", "body"}, "address like '%" + address + "%'", null, "_id desc");
            String info = "";
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
        return null;
    }

    @Override
    public Map getParamInfo(int tag, Object obj) {
        return null;
    }

    @Override
    public byte[] getPostParams(int flag) {
        return new byte[0];
    }

    @Override
    public void toActivity(Object response, int flag, Object obj) {

    }

    @Override
    public void showLoading(int flag) {

    }

    @Override
    public void hideLoading(int flag) {

    }

    @Override
    public void showError(int flag, Object obj) {

    }
}
