package cn.wwei.yzlsms.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Administrator on 2017-05-04.
 *
 * @author wwei
 */
public class SendTableBean implements Serializable{

    private int yzl_id;
    private String phone_send,phone_user;
    private long time;

    public int getYzl_id() {
        return yzl_id;
    }

    public void setYzl_id(int yzl_id) {
        this.yzl_id = yzl_id;
    }

    public String getPhone_send() {
        return phone_send;
    }

    public void setPhone_send(String phone_send) {
        this.phone_send = phone_send;
    }

    public String getPhone_user() {
        if (TextUtils.isEmpty(phone_user)) return "";
        if (phone_user.startsWith("+86")) return phone_user.substring(3,phone_user.length());
        if (phone_user.startsWith("0086")) return phone_user.substring(4,phone_user.length());
        return phone_user;
    }

    public void setPhone_user(String phone_user) {
        this.phone_user = phone_user;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "{" +
                "yzl_id=" + yzl_id +
                ", phone_send='" + phone_send + '\'' +
                ", phone_user='" + phone_user + '\'' +
                ", time=" + time +
                '}';
    }
}
