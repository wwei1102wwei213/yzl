package cn.wwei.yzlsms.biz;

import java.util.Map;

/**
 * Created by Administrator on 2016/4/21.
 *
 * 发送Http请求的接口
 *
 *
 * @author wwei
 */
public interface HttpResponseInterface {

    Map getParamInfo(int tag, Object obj);

    byte[] getPostParams(int flag);

    void toActivity(Object response, int flag, Object obj);

    void showLoading(int flag);

    void hideLoading(int flag);

    void showError(int flag, Object obj);

}
