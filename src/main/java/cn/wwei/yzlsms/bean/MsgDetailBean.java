package cn.wwei.yzlsms.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Administrator on 2017-05-01.
 */
public class MsgDetailBean implements Serializable{

    private String phone,body,sid;
    private int finish;
    private long time;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getFinish() {
        return finish;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPhone() {
        if (TextUtils.isEmpty(phone)) return "";
        if (phone.startsWith("+86")) return phone.substring(3,phone.length());
        if (phone.startsWith("0086")) return phone.substring(4,phone.length());
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        if (TextUtils.isEmpty(body)) body = "";

        this.body = body;
    }

    @Override
    public String toString() {
        return "MsgDetailBean{" +
                "time='" + time + '\'' +
                ", phone='" + phone + '\'' +
                ", body='" + body + '\'' +
                ", sid='" + sid + '\'' +
                ", finish=" + finish +
                '}';
    }
}
