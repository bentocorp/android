package com.bentonow.bentonow.Utils;

import android.location.Address;

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

        return sAddress;
    }
}
