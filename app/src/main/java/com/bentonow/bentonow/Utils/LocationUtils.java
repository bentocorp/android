package com.bentonow.bentonow.Utils;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.bentonow.bentonow.controllers.BentoApplication;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Jose Torres on 9/9/15.
 */
public class LocationUtils {

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
            if (sAddress.length() > 0)
                sAddress += ", ";

            if (i == 0)
                sAddress += mAddress.getSubThoroughfare() + " " + mAddress.getThoroughfare();
            else
                sAddress += mAddress.getAddressLine(i);
        }
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
        List<Address> matches;
        Address mAddress = null;
        Geocoder geoCoder = new Geocoder(BentoApplication.instance);

        try {
            matches = geoCoder.getFromLocation(mCurrentLocation.latitude, mCurrentLocation.longitude, 1);
            if (matches != null && !matches.isEmpty())
                mAddress = matches.get(0);
        } catch (Exception e) {
            DebugUtils.logError("getAddressFromLocation()", "scanCurrentLocation() " + e);
        }


        return mAddress;
    }
}

