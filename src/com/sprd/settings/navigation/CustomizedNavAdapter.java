package com.sprd.settings.navigation;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.android.settings.R;

public class CustomizedNavAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> listItems;
    private int[] softkeyImageRes = null;
    private boolean isNavHide = false;
    public boolean isNavHide() {
        return isNavHide;
    }

    public void setListItems(List<Map<String, Object>> listItems) {
        this.listItems = listItems;
    }

    public CustomizedNavAdapter(Context context, List<Map<String, Object>> items, int[] keyImageRes) {
        this.context = context;
        this.listItems = items;
        this.softkeyImageRes = keyImageRes;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.customized_nav_bar_item, null);
            holder = new ViewHolder();
            holder.rb = (RadioButton) convertView.findViewById(R.id.button);
            holder.keyImage = (ImageView) convertView.findViewById(R.id.keyImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.rb.setChecked((Boolean) getListItems().get(position).get("checked"));
        holder.keyImage.setImageResource(softkeyImageRes[position]);
        return convertView;
    }

    public Context getContext(){
        return context;
    }

    public List<Map<String, Object>> getListItems(){
        return listItems;
    }

    //UNISOC: Modify for bug 1160024
    public final static class ViewHolder {
        public ImageView keyImage;
        public RadioButton rb;
    }
}
