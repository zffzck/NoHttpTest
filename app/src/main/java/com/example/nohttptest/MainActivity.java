package com.example.nohttptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nohttptest.bean.MainBean;
import com.example.nohttptest.network.NetRequest;
import com.example.nohttptest.network.RequestResult;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;

import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private final static String HTTP_URL = "http://DisasterEarlyWarning.api.juhe.cn/jhapi/weather/provincelist";   //接口地址
    private ListView mListView;                //列表控件
    private MainJsonAdaptet mainJsonAdaptet;  //Json适配器
    private MainBeanAdaptet mBeanAdapter;    //javaBean适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NoHttp.initialize(getApplication());   //NoHttp全局声明
        initView();        //初始化控件
//      getJsonData();    //Json数据设置
        getBeanData();   //javaBean数据设置
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);    //获取列表ID

//      mainJsonAdaptet = new MainJsonAdaptet(this);    //获取mainJsonAdaptet对象
//      mListView.setAdapter(mainJsonAdaptet);         //传入mainJsonAdaptet适配器

        mBeanAdapter = new MainBeanAdaptet(this);  //获取MainBeanAdaptet对象
        mListView.setAdapter(mBeanAdapter);       //传入mBeanAdapter适配器
    }

    public void getJsonData() {
        new NetRequest<JSONObject>() {
            @Override
            public HashMap<String, Object> getParams(HashMap params) {
                params.put("key", "8208b05c4480b13ecdeb69f8cfcc3ab1");
                return params;
            }
        }.requestJsonObject(HTTP_URL, RequestMethod.GET, new RequestResult<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                mainJsonAdaptet.setData(data.optJSONArray("result"));
            }

            @Override
            public void onFailure(int code, String message) {
                Toast.makeText(MainActivity.this, "网络请求错误", Toast.LENGTH_SHORT).show();
            }
        }).setDebug(new Exception());
    }

    private void getBeanData() {
        new NetRequest<MainBean>() {
            @Override
            public HashMap<String, Object> getParams(HashMap<String, Object> params) {
                params.put("key", "8208b05c4480b13ecdeb69f8cfcc3ab1");
                return params;
            }
        }.requestBean(HTTP_URL, RequestMethod.GET, new RequestResult<MainBean>() {
            @Override
            public void onSuccess(MainBean data) {
                mBeanAdapter.setData(data.getResult());
            }

            @Override
            public void onFailure(int code, String message) {
                Toast.makeText(MainActivity.this, "网络请求错误", Toast.LENGTH_SHORT).show();
            }
        }).setDebug(new Exception());
    }
}
