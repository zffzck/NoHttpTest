package com.example.nohttptest.network;

import android.util.Log;

import com.google.gson.Gson;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>封装NoHttp，方便调试信息显示</p>
 * Created by Ftevxk on 2016/5/27.
 */
public abstract class NetRequest<T> {
    private String mBaseHost = "", mDebugMsg = "";
    private int mWhat;
    private Exception mDebugException;
    private RequestQueue mRequestQueue;
    private Gson mGson;
    private boolean isDebug;
    private HashMap<String, Object> mParams;
    private HashMap<String, String> mHeader;

    /**
     * <p>重写addHeader方法添加请求头</p>
     * <p>重写getParams方法添加请求参数</p>
     */
    public NetRequest() {
        this(0);
    }

    /**
     * @param what 队列位置
     */
    public NetRequest(int what) {
        mWhat = what;
        mRequestQueue = NoHttp.newRequestQueue();
        mGson = new Gson();
    }

    /**
     * 设置是否显示请求信息
     * @param debug 不为null开启调试，显示请求url和请求成功时的json数据
     *              Exception未设置Message也可用--查看LogCat信息
     */
    public NetRequest setDebug(Exception debug) {
        if (debug != null) {
            isDebug = true;
            mDebugException = debug;
            mDebugMsg = debug.getMessage() != null ? debug.getMessage() : "";
        } else {
            isDebug = false;
        }
        return this;
    }

    /**
     * 添加请求头
     */
    public HashMap<String, String> addHeader(HashMap<String, String> header) {
        return this.mHeader;
    }

    /**
     * 添加参数
     */
    public abstract HashMap<String, Object> getParams(HashMap<String, Object> params);

    public void setBaseHost(String host) {
        this.mBaseHost = host;
    }

    /**
     * 自定义请求方式
     */
    public NetRequest customRequest(Request<T> request, OnResponseListener<T> responseListener) {
        customRequest(mWhat, request, responseListener);
        return this;
    }

    public NetRequest customRequest(int what, Request<T> request, OnResponseListener<T> responseListener) {
        mRequestQueue.add(what, request, responseListener);
        return this;
    }

    /**
     * 返回请求队列
     */
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public NetRequest setWhat(int what) {
        this.mWhat = what;
        return this;
    }

    public int getWhat() {
        return mWhat;
    }

    public Gson getGson() {
        return mGson;
    }

    /**
     * 必须设置泛型
     *
     * @param mUrl     请求url
     * @param method   请求方法
     * @param listener 回调监听
     */
    public NetRequest requestBean(final String mUrl, RequestMethod method, final RequestResult<T> listener) {
        // 根据泛型转换class
        // error: requestBean必须设置泛型
        final Class<T> clazz = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        //url简单处理
        final String url;
        if (!mUrl.toLowerCase().contains("http://")) {
            url = mBaseHost + mUrl;
        } else {
            url = mUrl;
        }
        //请求对象
        final Request<JSONObject> request = NoHttp.createJsonObjectRequest(url, method);
        //请求头
        mHeader = addHeader(new HashMap<String, String>());
        //请求参数
        mParams = getParams(new HashMap<String, Object>());
        if (mParams != null && !mParams.isEmpty()) {
            JSONObject json = new JSONObject(mParams);
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                String key = it.next();
                String value = json.optString(key);
                request.add(key, value);
            }
        }
        if (mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        mRequestQueue.add(mWhat, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_START========================================");
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求发起位置:\n" + NetUtils.callMethodAndLine(mDebugException));
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求URL:\n" + url);
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求参数:\n" + NetUtils.getAddParams(mParams));
                    if (mHeader != null && !mHeader.isEmpty()) {
                        Log.d("NetRequest" + "--" + mDebugMsg, "请求头:\n" + mHeader);
                    }
                }
                if (listener != null) listener.onStart();
            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "返回结果:\n" + NetUtils.logJson(response.get().toString()));
                }
                if (response.get() != null) {
                    if (listener != null) {
                        listener.onSuccess(mGson.fromJson(response.get().toString(), clazz));
                    }
                } else {
                    if (listener != null) listener.onFailure(RequestResult.DATA_NULL, "null");
                }
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_END========================================");
                }
                if (listener != null) listener.onFinish();
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception e, int responseCode, long networkMillis) {
                Log.e("NetRequest" + "--" + mDebugMsg, "错误信息:\n" + e.getMessage());
                if (listener != null) listener.onFailure(responseCode, e.getMessage());
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_END========================================");
                }
                if (listener != null) listener.onFinish();
            }

            @Override
            public void onFinish(int what) {
                if (listener != null) listener.onSendEnd();
            }
        });
        return this;
    }

    /**
     * @param mUrl     请求url
     * @param method   请求方法
     * @param listener 回调监听
     */
    public NetRequest requestJsonObject(final String mUrl, RequestMethod method, final RequestResult<JSONObject> listener) {
        final String url;
        if (!mUrl.toLowerCase().contains("http://")) {
            url = mBaseHost + mUrl;
        } else {
            url = mUrl;
        }
        //请求对象
        final Request<JSONObject> request = NoHttp.createJsonObjectRequest(url, method);
        //请求头
        mHeader = addHeader(new HashMap<String, String>());
        //请求参数
        mParams = getParams(new HashMap<String, Object>());

        if (mParams != null && !mParams.isEmpty()) {
            JSONObject json = new JSONObject(mParams);
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                String key = it.next();
                String value = json.optString(key);
                request.add(key, value);
            }
        }
        if (mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        mRequestQueue.add(mWhat, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_START========================================");
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求发起位置:\n" + NetUtils.callMethodAndLine(mDebugException));
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求URL:\n" + url);
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求参数:\n" + NetUtils.getAddParams(mParams));
                    if (mHeader != null && !mHeader.isEmpty()) {
                        Log.d("NetRequest" + "--" + mDebugMsg, "请求头:\n" + mHeader);
                    }
                }
                if (listener != null) listener.onStart();
            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "返回结果:\n" + NetUtils.logJson(response.get().toString()));
                }
                if (listener != null) listener.onSuccess(response.get());
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_END========================================");
                }
                if (listener != null) listener.onFinish();
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception e, int responseCode, long networkMillis) {
                Log.e("NetRequest" + "--" + mDebugMsg, "错误信息:\n" + e.getMessage());
                if (listener != null) listener.onFailure(responseCode, e.getMessage());
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_END========================================");
                }
                if (listener != null) listener.onFinish();
            }

            @Override
            public void onFinish(int what) {
                if (listener != null) listener.onSendEnd();
            }
        });
        return this;
    }

    /**
     * @param mUrl     请求url
     * @param method   请求方法
     * @param listener 回调监听
     */
    public NetRequest requestJsonArray(final String mUrl, RequestMethod method, final RequestResult<JSONArray> listener) {
        final String url;
        if (!mUrl.toLowerCase().contains("http://")) {
            url = mBaseHost + mUrl;
        } else {
            url = mUrl;
        }
        //请求对象
        final Request<JSONArray> request = NoHttp.createJsonArrayRequest(url, method);
        //请求头
        mHeader = addHeader(new HashMap<String, String>());
        //请求参数
        mParams = getParams(new HashMap<String, Object>());

        if (mParams != null && !mParams.isEmpty()) {
            JSONObject json = new JSONObject(mParams);
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                String key = it.next();
                String value = json.optString(key);
                request.add(key, value);
            }
        }
        if (mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        mRequestQueue.add(mWhat, request, new OnResponseListener<JSONArray>() {
            @Override
            public void onStart(int what) {
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_START========================================");
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求发起位置:\n" + NetUtils.callMethodAndLine(mDebugException));
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求URL:\n" + url);
                    Log.d("NetRequest" + "--" + mDebugMsg, "请求参数:\n" + NetUtils.getAddParams(mParams));
                    if (mHeader != null && !mHeader.isEmpty()) {
                        Log.d("NetRequest" + "--" + mDebugMsg, "请求头:\n" + mHeader);
                    }
                }
                if (listener != null) listener.onStart();
            }

            @Override
            public void onSucceed(int what, Response<JSONArray> response) {
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "返回结果:\n" + NetUtils.logJson(response.get().toString()));
                }
                if (listener != null) listener.onSuccess(response.get());
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_END========================================");
                }
                if (listener != null) listener.onFinish();
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception e, int responseCode, long networkMillis) {
                Log.e("NetRequest" + "--" + mDebugMsg, "错误信息:\n" + e.getMessage());
                if (listener != null) listener.onFailure(responseCode, e.getMessage());
                if (isDebug) {
                    Log.d("NetRequest" + "--" + mDebugMsg, "=================================NET_END========================================");
                }
                if (listener != null) listener.onFinish();
            }

            @Override
            public void onFinish(int what) {
                if (listener != null) listener.onSendEnd();
            }
        });
        return this;
    }

    /**
     * 网络请求工具类
     */
    public static class NetUtils {
        /**
         * GET追加参数
         */
        public static String getAddParams(HashMap<String, Object> params) {
            String result = "";
            if (params != null && !params.isEmpty()) {
                int i = 0;
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (i == 0) {
                        result += "?" + entry.getKey() + "=" + entry.getValue();
                    } else {
                        result += "&" + entry.getKey() + "=" + entry.getValue();
                    }
                    i++;
                }
            }
            return result;
        }

        /**
         * @return log拼接这个方法就可以显示超链接
         */
        public static String callMethodAndLine(Exception e) {
            String meg = e.getMessage();
            String result;
            if (meg != null) {
                result = meg + "\nat ";
            } else {
                result = "at ";
            }
            StackTraceElement[] thisMethodStack = e.getStackTrace();
            for (StackTraceElement stack : thisMethodStack) {
                result += stack.getClassName() + ".";
                result += stack.getMethodName();
                result += "(" + stack.getFileName();
                result += ":" + stack.getLineNumber() + ")  \n";
                //return 只返回关键行
                return result;
            }
            return result;
        }

        /**
         * logcat打印json格式
         */
        public static String logJson(String json){
            try {
                int empty=0;
                char[]chs=json.toCharArray();
                StringBuilder stringBuilder=new StringBuilder();
                for (int i = 0; i < chs.length;) {
                    //若是双引号，则为字符串，下面if语句会处理该字符串
                    if (chs[i]=='\"') {

                        stringBuilder.append(chs[i]);
                        i++;
                        //查找字符串结束位置
                        for ( ; i < chs.length;) {
                            //如果当前字符是双引号，且前面有连续的偶数个\，说明字符串结束
                            if ( chs[i]=='\"'&&isDoubleSerialBackslash(chs,i-1)) {
                                stringBuilder.append(chs[i]);
                                i++;
                                break;
                            } else{
                                stringBuilder.append(chs[i]);
                                i++;
                            }

                        }
                    }else if (chs[i]==',') {
                        stringBuilder.append(',').append('\n').append(getEmpty(empty));

                        i++;
                    }else if (chs[i]=='{'||chs[i]=='[') {
                        empty++;
                        stringBuilder.append(chs[i]).append('\n').append(getEmpty(empty));

                        i++;
                    }else if (chs[i]=='}'||chs[i]==']') {
                        empty--;
                        stringBuilder.append('\n').append(getEmpty(empty)).append(chs[i]);

                        i++;
                    }else {
                        stringBuilder.append(chs[i]);
                        i++;
                    }


                }
                return stringBuilder.toString();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return json;
            }

        }

        /**
         * logJson --双斜杠
         */
        private static boolean isDoubleSerialBackslash(char[] chs, int i) {
            int count=0;
            for (int j = i; j >-1; j--) {
                if (chs[j]=='\\') {
                    count++;
                }else{
                    return count%2==0;
                }
            }

            return count%2==0;
        }
        /**
         * logJson --缩进
         */
        private static String getEmpty(int count){
            //默认每次缩进两个空格
            String empty="  ";
            StringBuilder stringBuilder=new StringBuilder();
            for (int i = 0; i < count; i++) {
                stringBuilder.append(empty) ;
            }

            return stringBuilder.toString();
        }
    }
}
