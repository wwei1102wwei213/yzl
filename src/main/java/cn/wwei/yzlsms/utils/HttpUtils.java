package cn.wwei.yzlsms.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2017-04-29.
 */
public class HttpUtils {



    public static String uploadPostMethod(String path, byte[] body) throws Exception{
        byte[] entitydata = body;
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
        OutputStream os = conn.getOutputStream();
        os.write(entitydata);
        os.flush();
        os.close();
        String req = "";
        if(conn.getResponseCode() == 200){

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                req += line;
            }
            System.out.println("req:" + req);

            try{
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
            return req.trim();
        }
        return null;
    }


    public static void main(String[] args) throws Exception{
        String params = "act=get_new_sms&time=2017-04-29+16%3a45%3a45&v_code=27b01fe064aad8177fd46b185836a030";
        String req = HttpUtils.uploadPostMethod("http://www.youzili.com/api/sendsms.aspx?act=get_new_sms&time=2017-04-29+16%3a45%3a45&v_code=27b01fe064aad8177fd46b185836a030",new byte[]{});
        System.out.println(req);
    }


}
