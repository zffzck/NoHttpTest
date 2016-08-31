package com.example.nohttptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/8/28.
 */
public class MainJsonAdaptet extends BaseAdapter {

    private JSONArray data;       //JSONArray数据
    private Context mContext;    //上下文

    /**
     * 构造方法
     * */
    public MainJsonAdaptet(Context context) {
        mContext = context;
        data = new JSONArray();  //创建对象
    }

    /**
     * 设置数据
     * */
    public void setData(JSONArray data) {
        this.data = data;
        notifyDataSetChanged();  //通知adapter调用GetView来刷新每个Item
    }

    @Override
    public int getCount() {
        return data.length();   //返回数组长度
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //判断View是否为空
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_main, null);  //获取Item布局
            holder = new ViewHolder(convertView);   //为初始化控件设置对象
            convertView.setTag(holder);            //传入获取到的控件
        }else {
            holder = (ViewHolder) convertView.getTag();  //不为空直接获取
        }
        JSONObject json = data.optJSONObject(position);         //
        String code = json.optString("code");                  //获取JSON数值
        holder.item_code.setText(code == null ? "" : code);   //如果是空显示为空，不为空传入code
        String city = json.optString("regionName");          //获取JSON数值
        holder.item_city.setText(city == null ? "" : city); //如果是空显示为空，不为空传入city
        return convertView;
    }

    /**
     * 初始化控件
     */
    public static class ViewHolder {
        public View rootView;
        public TextView item_code;
        public TextView item_city;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.item_code = (TextView) rootView.findViewById(R.id.item_code);
            this.item_city = (TextView) rootView.findViewById(R.id.item_city);
        }

    }
}
