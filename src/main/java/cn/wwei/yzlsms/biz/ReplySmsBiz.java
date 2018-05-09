package cn.wwei.yzlsms.biz;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.wwei.yzlsms.app.MyApplication;
import cn.wwei.yzlsms.app.YZLService;
import cn.wwei.yzlsms.bean.MsgDetailBean;
import cn.wwei.yzlsms.bean.SendDataBean;
import cn.wwei.yzlsms.bean.SendTableBean;
import cn.wwei.yzlsms.db.DBHelper;
import cn.wwei.yzlsms.utils.ExceptionUtils;
import cn.wwei.yzlsms.utils.LogCustom;

/**
 * Created by Administrator on 2017/5/7.
 */

public class ReplySmsBiz extends AsyncTask<Void,Void,Void> {

    public  Context context;
    private List<MsgDetailBean> data;
    private List<SendTableBean> beans;

    public ReplySmsBiz(Context context, List<SendTableBean> beans){
        this.context = context;
        this.beans = beans;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        long t = System.currentTimeMillis() - 3*1000*3600*24;
        data = DBHelper.getInstance().queryReplyTable(MyApplication.application,"time>"+t);
        if (data==null) data = new ArrayList<>();
        Uri uri = Uri.parse("content://sms/inbox");
        ContentResolver provider = context.getContentResolver();
        Cursor cur = provider.query(uri, new String[]{"_id", "address", "body","date"}, "read=0", null, "_id desc");
        List<MsgDetailBean> list = new ArrayList<>();
        String info = "";
        if (cur!=null){
            if (cur.moveToFirst()){
                do{
                    String str = "";
//                    String address = cur.getString(1);
                    if (getSendPhoneNum(cur.getString(1))){
                        MsgDetailBean mBean = new MsgDetailBean();
                        for (int j=0;j<cur.getColumnCount();j++){
                            switch (j){
                                case 0:
                                    mBean.setSid(cur.getString(0));
                                    break;
                                case 1:
                                    mBean.setPhone(cur.getString(1));
                                    break;
                                case 2:
                                    mBean.setBody(cur.getString(2));
                                    break;
                                case 3:
                                    mBean.setTime(Long.parseLong(cur.getString(3)));
                                    break;
                            }
                            str += cur.getColumnName(j)+"="+cur.getString(j)+",";
                            LogCustom.i("YZL","========>"+str);
                        }
                        list.add(mBean);
                    }

                }while (cur.moveToNext());
            }
        }
        cur.close();
        if (list.size()>0){
            for (MsgDetailBean mBean:list){
                if (data.size()>0){
                    boolean isHas = false;
                    for (MsgDetailBean mDbBean:data){
                        if (mBean.getSid().equals(mDbBean.getSid())){
                            isHas = true;
                            break;
                        }
                    }
                    if (!isHas){
                        DBHelper.getInstance().insertReplyTableItem(MyApplication.application,mBean);
                    }
                }else {
                    DBHelper.getInstance().insertReplyTableItem(MyApplication.application,mBean);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        ((YZLService)context).toStartFailList();
    }

    private boolean getSendPhoneNum(String phone){
        try {
            if (!TextUtils.isEmpty(phone)&&beans!=null&&beans.size()>0){
                LogCustom.i("YZL","========>"+beans.toString());
                for (int i=beans.size()-1;i>=0;i--){
                    if (phone.contains(beans.get(i).getPhone_user())){
                        return true;
                    }
                }
            }
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"getSendPhoneNum");
        }
        return false;
    }
}
