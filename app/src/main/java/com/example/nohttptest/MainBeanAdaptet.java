package com.example.nohttptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nohttptest.bean.MainBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/28.
 */
public class MainBeanAdaptet extends BaseAdapter{
    private List<MainBean.ResultBean> data;
    private Context mContext;

    /**
     * 构造方法
     * */
    public MainBeanAdaptet(Context context){
        mContext = context;
        data = new ArrayList<>();
    }

    public void setData(List<MainBean.ResultBean> data) {
        this.data = data;
        //通知adapter调用GetView来刷新每个Item
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        //获取数目
        return data.size();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_main, null);
            holder = new ViewHolder(convertView);  //为初始化控件设置对象
            convertView.setTag(holder);           //传入获取到的控件
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        MainBean.ResultBean bean = data.get(position);
        holder.item_code.setText(bean.getCode());
        holder.item_city.setText(bean.getRegionName());
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
