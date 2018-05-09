package cn.wwei.yzlsms;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;

import java.util.List;

import cn.wwei.yzlsms.bean.MsgYzlEntity;


/**
 * Created by Administrator on 2017-04-30.
 */
public class SendListAdapter extends BaseRecyclerAdapter<SendListAdapter.SendListViewHolder>{

    private Context context;
    private List<MsgYzlEntity> list;

    public SendListAdapter(Context context,List<MsgYzlEntity> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public SendListViewHolder getViewHolder(View view) {
        return new SendListViewHolder(view,false);
    }

    @Override
    public SendListViewHolder onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_send_list,parent,false);
        return new SendListViewHolder(view,true);
    }

    @Override
    public void onBindViewHolder(SendListViewHolder vh, int position, boolean isItem) {
        MsgYzlEntity entity = list.get(position);
        vh.order.setText(entity.getOrder_id()+"");
        if (entity.getNext()==0){
            vh.v_item.setBackgroundColor(context.getResources().getColor(R.color.color_white));
        }else {
            vh.v_item.setBackgroundColor(context.getResources().getColor(R.color.f4f4f4));
        }
        vh.phone.setText(TextUtils.isEmpty(entity.getSms_mobile())?"":entity.getSms_mobile());
        String send = "";
        switch (entity.getSend()){
            case 0:
                vh.send.setTextColor(context.getResources().getColor(R.color.red));
                send = "未处理";
                break;
            case 1:
                vh.send.setTextColor(context.getResources().getColor(R.color.color666));
                send = "等待处理";
                break;
            case 2:
                vh.send.setTextColor(context.getResources().getColor(R.color.green));
                send = "已发送";
                break;
            case 3:
                vh.send.setTextColor(context.getResources().getColor(R.color.blue));
                send = "发送失败";
                break;
            case 4:
                vh.send.setTextColor(context.getResources().getColor(R.color.red));
                send = "已终止";
                break;
        }
        vh.send.setText(send);
        String receive = "";
        switch (entity.getReceiver()){
            case 0:
                vh.receive.setTextColor(context.getResources().getColor(R.color.red));
                receive = "未接收";
                break;
            case 1:
                vh.receive.setTextColor(context.getResources().getColor(R.color.green));
                receive = "已接收";
                break;
            case 2:
                vh.receive.setTextColor(context.getResources().getColor(R.color.blue));
                receive = "接收失败";
                break;
            case 3:
                vh.receive.setTextColor(context.getResources().getColor(R.color.red));
                receive = "已终止";
                break;
        }
        vh.receive.setText(receive);
        vh.body.setText(TextUtils.isEmpty(entity.getSms_content())?"":entity.getSms_content());
        vh.send_time.setText(TextUtils.isEmpty(entity.getSend_time())?"":entity.getSend_time());
        vh.receive_time.setText(TextUtils.isEmpty(entity.getReceive_time())?"":entity.getReceive_time());
    }

    @Override
    public int getAdapterItemCount() {
        return list.size();
    }

    public void update(List<MsgYzlEntity> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public class SendListViewHolder extends RecyclerView.ViewHolder{

        private TextView send,receive,order,phone,body,send_time,receive_time;
        private View v_item;

        public SendListViewHolder(View itemView,boolean isItem){
            super(itemView);
            init(itemView,isItem);
        }

        public void init(View v,boolean isItem){
            if (isItem){
                order = (TextView)v.findViewById(R.id.tv_order);
                phone = (TextView)v.findViewById(R.id.tv_phone);
                body = (TextView)v.findViewById(R.id.tv_body);
                send = (TextView)v.findViewById(R.id.tv_send);
                receive = (TextView)v.findViewById(R.id.tv_receive);
                v_item = v.findViewById(R.id.v_item);
                send_time = (TextView)v.findViewById(R.id.tv_send_time);
                receive_time = (TextView)v.findViewById(R.id.tv_receive_time);
            }
        }

    }
}
