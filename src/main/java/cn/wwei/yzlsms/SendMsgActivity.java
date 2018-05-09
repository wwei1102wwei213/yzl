package cn.wwei.yzlsms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;

import java.util.ArrayList;
import java.util.List;

import cn.wwei.yzlsms.app.MyApplication;
import cn.wwei.yzlsms.bean.MsgYzlEntity;
import cn.wwei.yzlsms.utils.Const;


public class SendMsgActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        initView();
        initData();
    }

    private XRefreshView xrv;
    private RecyclerView rv;
    private SendListAdapter adapter;
    private TextView tv_send;
    private TextView tv_num_all;
    private TextView tv_num_finish;
    private TextView tv_num_wait;
    private TextView tv_num_fail;
    private void initView(){

        tv_num_all = (TextView)findViewById(R.id.tv_num_all);
        tv_num_finish = (TextView)findViewById(R.id.tv_num_finish);
        tv_num_wait = (TextView)findViewById(R.id.tv_num_wait);
        tv_num_fail = (TextView)findViewById(R.id.tv_num_fail);

        xrv = (XRefreshView)findViewById(R.id.xrv);
        rv = (RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        adapter = new SendListAdapter(this,new ArrayList<MsgYzlEntity>());
        rv.setAdapter(adapter);
        xrv.setPinnedTime(1000);
        xrv.setPullRefreshEnable(false);
        xrv.setPullLoadEnable(true);
        adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
        xrv.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh() {
                super.onRefresh();
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                toLoading();
            }
        });
        tv_send = (TextView)findViewById(R.id.tv_send);
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSend();
            }
        });
    }

    private void initData(){
        if (MyApplication.service==null){
            showToast("工作后台已停止，请重启APP");
            return;
        }
        MyApplication.service.setActivity(this);
    }

    private void toLoading(){
        if (MyApplication.service==null){
            showToast("工作后台已停止，请重启APP");
            loadingFinish();
            return;
        }
        if (MyApplication.service.isWork){
            showToast("正在处理，请稍后");
            loadingFinish();
            return;
        }
        if (!MyApplication.service.isWorkEmpty()){
            showToast("请处理完当前工作列表");
            loadingFinish();
            return;
        }
        if (MyApplication.service.isStartNextFlag()){
            showToast("正在自动获取下一列表,或正在取消获取,请稍后");
            loadingFinish();
            return;
        }
        MyApplication.service.getData(Const.GetNewSms);
    }

    public void loadingFinish(){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1500);
                }catch (Exception e){

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                xrv.stopLoadMore(true);
            }
        }.execute();
    }

    private void toSend(){
        if (MyApplication.service==null){
            showToast("工作后台已停止，请重启APP");
            return;
        }
        MyApplication.service.toSendMsg();
    }

    public void showStopDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示")
                .setMessage("确定终止列表发送?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (MyApplication.service!=null){
                            MyApplication.service.toStopList();
                        }
                    }
                })
                .setNegativeButton("点错了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create().show();
    }

    public void update(List<MsgYzlEntity> list){
        if (list==null||list.size()==0){

        }else {
            adapter.update(list);
        }
    }

    private AlertDialog dialog;
    public void showNextProgressDialog(){
        try {
            if (dialog==null){
                AlertDialog.Builder builder = new ProgressDialog.Builder(this);
                builder.setMessage("正在获取下一列表...");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (MyApplication.service!=null){
                            MyApplication.service.setNextTimeFlag(false);
                        }
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                /*dialog = new ProgressDialog(this);
                dialog.setMessage("正在获取下一列表...");
                dialog.setCancelable(false);*/
            }
            if (!dialog.isShowing()){
                dialog.show();
            }
        }catch (Exception e){

        }
    }

    public void cancelNextProgressDialig(){
        try {
            if (dialog!=null){
                dialog.dismiss();
            }
        }catch (Exception e){

        }
    }

    private void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApplication.service!=null){
            MyApplication.service.setIsShow(true);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (adapter!=null&&MyApplication.service!=null) {
            MyApplication.service.reStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MyApplication.service!=null){
            MyApplication.service.setIsShow(false);
        }
    }

    @Override
    public void finish() {

        if (MyApplication.service!=null&&MyApplication.service.isWork){
            showToast("正在处理列表，请稍后退出");
        }else {
            super.finish();
        }
    }

    //flag 1:发送中 0:发送  2:已暂停
    public void setSendBtnState(int flag){
        if (flag==1){
            tv_send.setText("发送中");
            tv_send.setTextColor(getResources().getColor(R.color.colorccc));
        }else if(flag==2){
            tv_send.setText("已暂停");
            tv_send.setTextColor(getResources().getColor(R.color.blue));
        } else {
            tv_send.setText("发送");
            tv_send.setTextColor(getResources().getColor(R.color.color_white));
        }
    }

    public void setListNum(int all,int finish,int wait,int fail){
        tv_num_all.setText(all+"");
        tv_num_finish.setText(finish+"");
        tv_num_wait.setText(wait+"");
        tv_num_fail.setText(fail+"");
    }

}