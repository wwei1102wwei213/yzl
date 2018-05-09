package cn.wwei.yzlsms.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Administrator on 2017-04-30.
 */
public class MsgYzlEntity implements Serializable{

//    private static final long serialVersionUID = 1L;

    /*public static final Parcelable.Creator<MsgYzlEntity> CREATOR = new Creator<MsgYzlEntity>() {
        @Override
        public MsgYzlEntity createFromParcel(Parcel parcel) {
            MsgYzlEntity entity = new MsgYzlEntity();
            entity.sms_id = parcel.readInt();
            entity.send = parcel.readInt();
            entity.receiver = parcel.readInt();
            entity.next = parcel.readInt();
            entity.sms_mobile = parcel.readString();
            entity.sms_content = parcel.readString();
            entity.send_time = parcel.readString();
            entity.receive_time = parcel.readString();
            entity.fail_ids = parcel.readString();
            return entity;
        }

        @Override
        public MsgYzlEntity[] newArray(int i) {
            return new MsgYzlEntity[i];
        }
    };*/

    private int sms_id,send,receiver,next;
    private String sms_mobile,sms_content,send_time,receive_time,fail_ids;

    public int getOrder_id() {
        return sms_id;
    }

    public void setOrder_id(int order_id) {
        this.sms_id = order_id;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public String getSms_mobile() {
        return sms_mobile;
    }

    public void setSms_mobile(String sms_mobile) {
        this.sms_mobile = sms_mobile;
    }

    public String getSms_content() {
        return sms_content;
    }

    public void setSms_content(String sms_content) {
        this.sms_content = sms_content;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
    }

    public String getReceive_time() {
        return receive_time;
    }

    public void setReceive_time(String receive_time) {
        this.receive_time = receive_time;
    }

    public String getFail_ids() {
        return fail_ids;
    }

    public void setFail_ids(String fail_ids) {
        this.fail_ids = fail_ids;
    }

    /*@Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(sms_id);
        parcel.writeInt(send);
        parcel.writeInt(receiver);
        parcel.writeInt(next);
        parcel.writeString(sms_mobile);
        parcel.writeString(sms_content);
        parcel.writeString(send_time);
        parcel.writeString(receive_time);
        parcel.writeString(fail_ids);
    }*/

    public int getSms_id() {
        return sms_id;
    }

    @Override
    public String toString() {
        return "MsgYzlEntity{" +
                "sms_id=" + sms_id +
                ", send=" + send +
                ", receiver=" + receiver +
                ", next=" + next +
                ", sms_mobile='" + sms_mobile + '\'' +
                ", sms_content='" + sms_content + '\'' +
                ", send_time='" + send_time + '\'' +
                ", receive_time='" + receive_time + '\'' +
                ", fail_ids='" + fail_ids + '\'' +
                '}';
    }
}
