package com.wsdcamp.anim;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class FadeInOut implements Animation.AnimationListener {
    Context context;
    View view;
    int animFadeIn;
    int animFadeOut;

    public FadeInOut(Context context, View view, int animFadeIn, int animFadeOut) {
        this.context = context;
        this.view = view;
        this.animFadeIn = animFadeIn;
        this.animFadeOut = animFadeOut;

        new FadeIn(context, view, animFadeIn, this);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new FadeOut(context, view, animFadeOut, null);
            }
        }, 2000);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
