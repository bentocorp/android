package com.wsdcamp.anim;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class FadeIn implements Animation.AnimationListener {

    View view;
    Animation.AnimationListener listener;

    public FadeIn (Context context, View view, int id, Animation.AnimationListener listener) {
        this.listener = listener;
        this.view = view;

        Animation anim = AnimationUtils.loadAnimation(context, id);
        anim.setAnimationListener(this);
        view.startAnimation(anim);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        view.setVisibility(View.GONE);
        if (listener != null) listener.onAnimationStart(animation);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        view.setVisibility(View.VISIBLE);
        if (listener != null) listener.onAnimationEnd(animation);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        if (listener != null) listener.onAnimationRepeat(animation);
    }
}
