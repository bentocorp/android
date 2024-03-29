package com.bentonow.bentonow.Utils.maps;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public static String getStringSecondsLeft(int iSeconds) {
        int hours = iSeconds / 3600;
        int minutes = (iSeconds % 3600) / 60;
        int seconds = iSeconds % 60;

        // return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return String.valueOf(minutes);
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


    public static Address getAddressFromString(String sAddress) {
        Address mAddress = null;
        List<Address> addresses = new ArrayList<>();
        Geocoder geoCoder = new Geocoder(BentoApplication.instance, Locale.US);

        try {
            addresses.addAll(geoCoder.getFromLocationName(sAddress, 1));
            if (!addresses.isEmpty())
                return addresses.get(0);

        } catch (Exception e) {
            DebugUtils.logError(TAG, "getAddressFromString()" + e);
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
        return ConstantUtils.optBearing.values()[(int) Math.floor(dRotation / 45)];
    }

    public static double getDistanceInTwoPoints(LatLng StartP, LatLng EndP) {
        double theta = StartP.longitude - EndP.longitude;
        double dist = Math.sin(deg2rad(StartP.latitude)) * Math.sin(deg2rad(EndP.latitude)) + Math.cos(deg2rad(StartP.latitude)) * Math.cos(deg2rad(EndP.latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        DebugUtils.logDebug(TAG, "Miles :: " + dist + "   KM ::  " + dist * 1.609344 + " Nautical ::  " + dist * 0.8684);

        dist = (dist * 1.609344);

        return dist;
    }

    public static ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        double dLat = 0, dLng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            dLat = ((double) lat / 1E5);
            dLng = ((double) lng / 1E5);
            //  DebugUtils.logDebug(TAG, "Latitude:: " + dLat + " Longitude:: " + dLng);
            LatLng p = new LatLng(dLat, dLng);
            poly.add(p);
        }

        DebugUtils.logDebug(TAG, "Size of Polyline:: " + poly.size());

        return poly;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double degToRad(double deg) {
        return deg * Math.PI / 180.0;
    }

    public static double radToDeg(double rad) {
        rad = rad * (180.0 / Math.PI);
        if (rad < 0) rad = 360.0 + rad;
        return rad;
    }

    public static double getRotationFromLocations(LatLng mLastLocation, LatLng mCurrentLocation) {
        double lat1 = LocationUtils.degToRad(mLastLocation.latitude);
        double lon1 = LocationUtils.degToRad(mLastLocation.longitude);

        double lon2 = LocationUtils.degToRad(mCurrentLocation.longitude);
        double lat2 = LocationUtils.degToRad(mCurrentLocation.latitude);

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double dBearing = Math.atan2(y, x);

        if (dBearing < 0) {
            dBearing += 2 * Math.PI;
        }

        dBearing = radToDeg(dBearing);

        DebugUtils.logDebug(TAG, "Bearing:: " + dBearing);

        return dBearing; // bearing
    }

}

