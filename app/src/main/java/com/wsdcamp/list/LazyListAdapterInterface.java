package com.wsdcamp.list;

import android.view.View;
import android.view.ViewGroup;

public interface LazyListAdapterInterface {
    int getCount();
    Object getItem(int position);
    long getItemId(int position);
    View getView(int position, View convertView, ViewGroup parent);
}
