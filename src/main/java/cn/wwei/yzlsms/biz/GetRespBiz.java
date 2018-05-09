package cn.wwei.yzlsms.biz;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.Map;

import cn.wwei.yzlsms.bean.BaseBean;
import cn.wwei.yzlsms.bean.SendDataBean;
import cn.wwei.yzlsms.utils.Const;
import cn.wwei.yzlsms.utils.LogCustom;
import cn.wwei.yzlsms.utils.MyHttpClientManager;


/**
 * Created by Administrator on 2017-04-29.
 */
public class GetRespBiz {

    private static final String TAG = "YZL";
    private HttpResponseInterface sendPage;
    private int flag;
    private Gson mGson;
    private Object obj;

    public GetRespBiz(HttpResponseInterface responseInterface,int flag){
        this.sendPage = responseInterface;
        this.flag = flag;
        this.mGson = new Gson();
        this.obj = null;
    }

    public GetRespBiz(HttpResponseInterface responseInterface,int flag,Object obj){
        this.sendPage = responseInterface;
        this.flag = flag;
        this.mGson = new Gson();
        this.obj = obj;
    }

    public void postResponse(){
        sendPage.showLoading(flag);
        MyHttpClientManager.postNewAsyn(getPostUrl(), new HashMap<String, String>(),
                new MyHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {
                        LogCustom.i(TAG, "请求失败：" + request.toString());
                        sendPage.showError(flag,obj);
                    }

                    @Override
                    public void onResponse(String response) {
                        sendPage.hideLoading(flag);
                        Object o = getGsonType(response);
                        if (o != null) {
                            LogCustom.i(TAG, "请求成功：" + o.toString());
                            sendPage.toActivity(o, flag, obj);
                        } else {
                            LogCustom.i(TAG, "请求成功但数据为空");
                            sendPage.showError(flag,obj);
                        }
                    }
                });
    }



    private String getPostUrl(){
        String url = null;
        switch (flag){
            case Const.GetNewSms:
            case Const.PostReceipt:
            case Const.PostReplySms:
            case Const.PostFailedOrderIds:
            case Const.ResetTestData:
                url = Const.IP_PORT;
                Map<String,String> map = sendPage.getParamInfo(flag, obj);
                String params = "";
                try {
                    for (String key:map.keySet()){
                        if (TextUtils.isEmpty(params)){
                            params += (key + "=" + map.get(key));
                        } else {
                            params += ("&"+key + "=" + map.get(key));
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                url += params;
                break;
        }
        LogCustom.i(TAG, "请求地址：" + url);
        return url;
    }


    private Object getGsonType(String response){
        Object o = null;
        LogCustom.i(TAG, "请求数据：" + response);
        try {
            switch (flag) {
                case Const.GetNewSms:
                    o = mGson.fromJson(response, SendDataBean.class);
                    break;
                case Const.PostReceipt:
                case Const.PostReplySms:
                case Const.PostFailedOrderIds:
                case Const.ResetTestData:
                    o = mGson.fromJson(response, BaseBean.class);
                    break;

            }
        } catch (com.google.gson.JsonParseException e) {
            LogCustom.i(TAG, "请求成功,JSON异常" + e.toString());
            try {
                if (flag==Const.PostReplySms){
                    Toast.makeText((Context)sendPage, response+","+getPostUrl(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex){

            }

        }
        return o;
    }

    /*public static void main(String[] args){
        try {
            String key = "";
            String value = URLDecoder.decode(key, "UTF-8");
            System.out.print(value);
        }catch (Exception e){

        }
    }*/
}
