package cn.wwei.yzlsms.db;

import com.ta.util.db.TASQLiteDatabase;

import java.util.List;

import cn.wwei.yzlsms.app.MyApplication;
import cn.wwei.yzlsms.bean.MsgDetailBean;
import cn.wwei.yzlsms.bean.MsgYzlEntity;
import cn.wwei.yzlsms.bean.SendTableBean;
import cn.wwei.yzlsms.utils.ExceptionUtils;
import cn.wwei.yzlsms.utils.LogCustom;


/**
 * Created by Administrator on 2016/3/30.
 *
 *
 *
 *
 * @author wwei
 */
public class DBHelper {

    private static final String TAG = "YZL";

    private DBHelper(){}

    private static DBHelper instance = null;

    /***
     * 单例模式，注意要加同步锁
     *
     * @return ProvinceDatabase对象
     */
    public static DBHelper getInstance(){
        if(instance==null){
            synchronized(DBHelper.class){
                if(instance==null){
                    instance = new DBHelper();
                }
            }
        }
        return instance;
    }

    //创建发送接收手机号对应表
    public void initCreateSendTable(MyApplication application){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(SendTableBean.class)){
                tasqLiteDatabase.creatTable(SendTableBean.class);
                LogCustom.i(TAG, "SendTableBean表已创建");
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e, "initCreateSendTable");
        }
    }

    //创建短信回复表
    public void initCreateReplyTable(MyApplication application){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(MsgDetailBean.class)){
                tasqLiteDatabase.creatTable(MsgDetailBean.class);
                LogCustom.i(TAG, "MsgDetailBean表已创建");
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e, "initCreateReplyTable");
        }
    }

    public void initDropSendTable(MyApplication application){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if(tasqLiteDatabase.hasTable(SendTableBean.class)){
                tasqLiteDatabase.dropTable(SendTableBean.class);
                LogCustom.i(TAG, "SendTableBean表已清除");
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"initDropSendTable");
        }
    }

    public List<SendTableBean> querySendTable(MyApplication myApplication){
        TASQLiteDatabase tasqLiteDatabase = myApplication.getSQLiteDatabasePool().getSQLiteDatabase();
        long t = System.currentTimeMillis() - 3*1000*3600*24;
        List<SendTableBean> list = tasqLiteDatabase.query(SendTableBean.class, false, "time>"+t, null, null, null, null);
        myApplication.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        return list;
    }

    public List<MsgDetailBean> queryReplyTable(MyApplication myApplication,String where){
        TASQLiteDatabase tasqLiteDatabase = myApplication.getSQLiteDatabasePool().getSQLiteDatabase();
        List<MsgDetailBean> list = tasqLiteDatabase.query(MsgDetailBean.class, false, where, null, null, null, null);
        myApplication.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        return list;
    }

    public void insertReplyTableItem(MyApplication application,MsgDetailBean bean){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            List<MsgDetailBean> list = tasqLiteDatabase.query(MsgDetailBean.class, false, "sid="+bean.getSid(), null, null, null, null);
            if (list==null||list.size()==0){
                tasqLiteDatabase.insert(bean);
            }else {
                tasqLiteDatabase.update(bean,"sid="+bean.getSid());
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"insertReplyTableItems");
        }
    }

    public void updataReplyTableItem(MyApplication application,MsgDetailBean bean){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            tasqLiteDatabase.update(bean,"sid="+bean.getSid());
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"insertReplyTableItems");
        }
    }

    public void deleteSendTableItem(MyApplication application,SendTableBean bean){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            tasqLiteDatabase.delete(bean);
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"deleteSendTableItem");
        }
    }

    public void insertSendTableItems(MyApplication application,List<MsgYzlEntity> list){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            for (MsgYzlEntity entity:list){
                SendTableBean bean = new SendTableBean();
                bean.setYzl_id(entity.getOrder_id());
                bean.setPhone_send(MyApplication.PHONE_NUM);
                bean.setPhone_user(entity.getSms_mobile());
                bean.setTime(System.currentTimeMillis());
                tasqLiteDatabase.insert(bean);
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"insertSendTableItems");
        }
    }

    public void deleteSendTableItems(MyApplication application,List<SendTableBean> beans){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            for (SendTableBean bean:beans){
                tasqLiteDatabase.delete(bean);
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"deleteSendTableItems");
        }
    }

    /*public void updataPushTable(ZYApplication application,PushNewEntity entity){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            tasqLiteDatabase.delete(PushNewEntity.class,"time="+entity.getTime());
            entity.setRead(1);
            tasqLiteDatabase.insert(entity);
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e,"UpdatePush");
            LogCustom.i("ZYS","PushEntity更新失败");
        }
    }*/


}
