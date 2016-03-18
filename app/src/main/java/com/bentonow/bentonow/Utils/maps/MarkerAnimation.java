package com.bentonow.bentonow.Utils.maps;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.util.Property;

/**
 * Created by kokusho on 3/4/16.
 */
public class MarkerAnimation {

    public static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, float fRotation, final LatLngInterpolator latLngInterpolator) {
        marker.setRotation(fRotation);
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 2000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, int duration, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                marker.setPosition(newPosition);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(AndroidUtil.getMillisFromSeconds(duration));
        valueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void animateMarkerToICS(Marker mMarkerDriver, LatLng finalPosition, float fRotation, final LatLngInterpolator latLngInterpolator, String sSnippet, int iAnimation) {
        // marker.setRotation(fRotation);

        if (fRotation == 0) {

        } else if (fRotation <= 45) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_up_right));
        } else if (fRotation <= 90) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_up));
        } else if (fRotation <= 135) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_left_up));
        } else if (fRotation <= 180) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_left));
        } else if (fRotation <= 225) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_down_left));
        } else if (fRotation <= 270) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_down));
        } else if (fRotation <= 315) {
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_down_righ));
        } else
            mMarkerDriver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car));

        mMarkerDriver.setSnippet(sSnippet);
        mMarkerDriver.showInfoWindow();

        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(mMarkerDriver, property, typeEvaluator, finalPosition);
        animator.setDuration(iAnimation);
        animator.start();
    }
}