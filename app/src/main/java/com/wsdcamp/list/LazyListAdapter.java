package com.wsdcamp.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LazyListAdapter extends BaseAdapter {

    LazyListAdapterInterface adapter;

    public LazyListAdapter (LazyListAdapterInterface adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getCount() {
        return adapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return adapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return adapter.getView(position, convertView, parent);
    }
}
