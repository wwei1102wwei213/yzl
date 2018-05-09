package cn.wwei.yzlsms.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017-04-30.
 */
public class SendDataBean implements Serializable{

    private String error;
    private List<MsgYzlEntity> sms_list;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<MsgYzlEntity> getSms_list() {
        return sms_list;
    }

    public void setSms_list(List<MsgYzlEntity> sms_list) {
        this.sms_list = sms_list;
    }

    @Override
    public String toString() {
        return "{" +
                "error:'" + error + '\'' +
                ", sms_list:" + sms_list +
                '}';
    }

    /*public static void main(String[] args){
        int hour = new Date(System.currentTimeMillis()).getHours();
        System.out.print("=======>"+hour);
        hour = new Date(System.currentTimeMillis()+12*60*60*1000).getHours();
        System.out.print("=======>"+hour);
    }*/

}
