package cn.wwei.yzlsms.utils;

/**
 * Created by Administrator on 2017-04-29.
 */
public class Const {

    public static final String IP_PORT = "http://www.youzili.com/api/sendsms.aspx?";
    public static final int GetNewSms = 1;
    public static final int PostReceipt = 2;
    public static final int PostReplySms = 3;
    public static final int PostFailedOrderIds = 4;
    public static final int ResetTestData = 5;

    public static final String RESULT = "0";

    public static final String MD5_KEY = "86bf2c32c569a134c8a5a5698b3ecd79";

    public static final String SERVICE_START = "Intent_Service_Start";
    public static final String SERVICE_STOP = "Intent_Service_Stop";

    public static final String ACTION_SENT_HEAD_INTENT = "head_intent_sent";
    public static final String ACTION_RECEIVER_HEAD_INTENT = "head_intent_receiver";
    public static final String PENDING_TAG_SENT_HEAD_INTENT = "head_pending_intent_sent";
    public static final String PENDING_TAG_RECEIVER_HEAD_INTENT = "head_pending_intent_receiver";



    //SharedPreferences中时间间隔的键值
    public static final String SP_SPACE_TIME = "SP_SPACE_TIME";
    public static final String SP_PHONE_NUM = "SP_PHONE_NUM";
    public static final String SP_RANDOM_NUM = "SP_RANDOM_NUM";
    public static final String SP_TEST_FLAG = "SP_TEST_FLAG";

    public static final String SERVICE_LOAD_MORE = "SERVICE_LOAD_MORE";

    public static final String INTENT_LIST_POSITION = "INTENT_LIST_POSITION";
    public static final String INTENT_LIST_FLAG = "INTENT_LIST_FLAG";
    public static final String INTENT_LIST_STATUS = "INTENT_LIST_STATUS";

//    public static final String S_GetNewSms = IP_PORT + "";

}
