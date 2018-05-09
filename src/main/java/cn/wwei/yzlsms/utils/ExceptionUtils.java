package cn.wwei.yzlsms.utils;

import android.content.Context;
import android.util.Log;

/**
 *
 * 异常信息处理工具类
 *
 * Created by Administrator on 2016/3/4.
 *
 * @author wwei
 *
 */
public class ExceptionUtils {
    public static void ExceptionSend(Exception e){
        Log.e("EXC",e.toString());
    }

    public static void ExceptionSend(Exception e,String where){
        try {
            Log.e("EXCO","异常位置：" + where + "\n" + "异常信息：" + e.toString());
            StackTraceElement[] stack = e.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<stack.length;i++){
                builder.append(stack[i].toString()+"\n");
            }
            Log.e("EXCO", "异常位置：" + where + "\n" + "异常信息：" + builder.toString());
        }catch (Exception ex){
            e.printStackTrace();
        }
    }

    public static void ExceptionToUM(Exception e,Context context,String where){
        try {
            Log.e("EXCO", "异常位置：" + where + "\n" + "异常信息：" + e.toString());

            StackTraceElement[] stack = e.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<stack.length;i++){
                builder.append(stack[i].toString()+"\n");
            }
            Log.e("EXCO", "异常位置：" + where + "\n" + "异常信息：" + builder.toString());
        }catch (Exception ex){
            e.printStackTrace();
        }
    }

}
