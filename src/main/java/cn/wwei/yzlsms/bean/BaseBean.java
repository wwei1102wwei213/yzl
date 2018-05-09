package cn.wwei.yzlsms.bean;

/**
 * Created by Administrator on 2017-04-29.
 */
public class BaseBean {

    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "BaseBean{" +
                "error='" + error + '\'' +
                '}';
    }
}
