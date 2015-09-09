package com.bentonow.bentonow.Utils;

import android.location.Address;

/**
 * Created by Jose Torres on 9/9/15.
 */
public class LocationUtils {

    public static String getFullAddress(Address mAddress) {
        if (mAddress == null)
            return "";

        String addrss = "";

        for (int i = 0; i < mAddress.getMaxAddressLineIndex(); ++i) {
            if (addrss.length() > 0) addrss += ", ";
            addrss += mAddress.getAddressLine(i);
        }

        return addrss;
    }

    public static String getStreetAddress(Address mAddress) {
        if (mAddress == null) return "";
        return mAddress.getThoroughfare() + ", " + mAddress.getSubThoroughfare();
    }
}
