package com.example.nohttptest.network;

public abstract class RequestResult<T>{
    /* 数据结果为null的返回code */
    public static final int DATA_NULL = -0x123;
    /* 请求开始 */
    public void onStart(){};
    /* 请求成功 */
    public abstract void onSuccess(T data);
    /* 请求失败 */
    public abstract void onFailure(int code, String message);
    /* 请求发送结束(请求结束) */
    public void onSendEnd(){};
    /* 请求返回结果结束(整个流程结束) */
    public void onFinish(){};
}
