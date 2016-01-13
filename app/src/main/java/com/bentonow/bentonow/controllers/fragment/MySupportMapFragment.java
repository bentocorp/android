package com.bentonow.bentonow.controllers.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bentonow.bentonow.listener.OnCustomDragListener;
import com.bentonow.bentonow.ui.TouchableWrapperView;
import com.google.android.gms.maps.SupportMapFragment;

public class MySupportMapFragment extends SupportMapFragment {
    public View mOriginalContentView;
    public TouchableWrapperView mTouchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapperView(getActivity());
        mTouchView.addView(mOriginalContentView);
        return mTouchView;
    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }

    public void setOnDragListener(OnCustomDragListener mOnDragListener) {
        mTouchView.setOnDragListener(mOnDragListener);
    }

}