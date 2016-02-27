package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose Torres on 9/9/15.
 */
public class LocationUtils {

    public static final String TAG = "LocationUtils";

    public static String getFullAddress(Address mAddress) {
        if (mAddress == null)
            return "";

        String sAddress = "";

        for (int i = 0; i < mAddress.getMaxAddressLineIndex(); ++i) {
            if (sAddress.length() > 0) sAddress += ", ";
            sAddress += mAddress.getAddressLine(i);
        }

        return sAddress;
    }

    public static String getStreetAddress(Address mAddress) {
        if (mAddress == null) return "";
        return mAddress.getThoroughfare() + ", " + mAddress.getSubThoroughfare();
    }

    public static String getCustomAddress(Address mAddress) {
        if (mAddress == null)
            return "";

        String sAddress = "";

        for (int i = 0; i < mAddress.getMaxAddressLineIndex(); ++i) {
            String sLine;
            if (sAddress.length() > 0)
                sAddress += ", ";

            if (i == 0)
                sLine = mAddress.getSubThoroughfare() + " " + mAddress.getThoroughfare();
            else
                sLine = mAddress.getAddressLine(i);

            if (sLine != null)
                sAddress += sLine;
        }

        sAddress = sAddress.replace("null", "");

        DebugUtils.logDebug("getCustomAddress()", sAddress);

        return sAddress;
    }

    public static Address getAddressFromLocation(Location mCurrentLocation) {
        List<Address> matches;
        Address mAddress = null;
        Geocoder geoCoder = new Geocoder(BentoApplication.instance);

        try {
            matches = geoCoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            if (matches != null && !matches.isEmpty())
                mAddress = matches.get(0);
        } catch (Exception e) {
            DebugUtils.logError("getAddressFromLocation()", "scanCurrentLocation() " + e);
        }


        return mAddress;
    }


    public static Address getAddressFromLocation(LatLng mCurrentLocation) {
        List<Address> addresses = new ArrayList<>();
        Address mAddress = null;
        Geocoder geoCoder = new Geocoder(BentoApplication.instance);

        try {
            addresses = geoCoder.getFromLocation(mCurrentLocation.latitude, mCurrentLocation.longitude, 1);
        } catch (Exception e) {
            DebugUtils.logError("getAddressFromLocation()", "scanCurrentLocation() " + e);
        }

        if (!addresses.isEmpty()) {
            mAddress = addresses.get(0);
        }

        return mAddress;
    }

    public static boolean isGpsEnable(final FragmentActivity mActivity) {
        LocationManager mLocManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        return mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void showGpsDialog(final FragmentActivity mActivity) {
        ConfirmationDialog mDialog = new ConfirmationDialog(mActivity, "Enable GPS", "GPS is disabled in your device. Enable it?");
        mDialog.addAcceptButton("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                try {
                    mActivity.startActivity(callGPSSettingIntent);

                } catch (Exception ex) {

                }
            }
        });
        mDialog.show();
    }

    public static ConstantUtils.optBearing getBearingFromRotation(double dRotation) {
        if (dRotation > 360)
            dRotation = 360;
        return ConstantUtils.optBearing.values()[(int) Math.floor(dRotation / 90)];
    }

    public static double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        double result = Radius * c;
        DebugUtils.logDebug(TAG, "Radius Value:: "+  result + "   KM  " + kmInDec + " Meter   " + meterInDec);
        return Radius * c;
    }
}

