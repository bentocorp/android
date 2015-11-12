package com.bentonow.bentonow.Utils;

import android.location.Location;
import android.os.Bundle;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jose Torres on 10/1/15.
 */
public class GoogleLocationUtil {

    public static final String TAG = "GoogleLocationUtil";

    private static LocationRequest mLocationRequest;
    private static GoogleApiClient mGoogleApiClient;


    public static synchronized GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(BentoApplication.instance)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            DebugUtils.logDebug(TAG, "buildGoogleApiClient() onConnected:");
                            startLocationUpdates();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            DebugUtils.logDebug(TAG, "buildGoogleApiClient() onConnectionSuspended: " + i);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            DebugUtils.logDebug(TAG, "buildGoogleApiClient() " + connectionResult.toString());
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
        return mGoogleApiClient;
    }

    public static void startLocationUpdates() {
        if (getGoogleApiClient().isConnected())
            LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), getLocationRequest(), new LocationListener() {
                @Override
                public void onLocationChanged(Location mLocation) {
                    DebugUtils.logDebug(TAG, "startLocationUpdates", "onLocationChanged() " + mLocation.toString());

                    if (BuildConfig.DEBUG && BentoNowUtils.B_KOKUSHO_TESTING) {
                        LocationUtils.mCurrentLocation = new LatLng(37.76573527907957, -122.41834457963704);
                    } else {
                        LocationUtils.mCurrentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    }

                    Order.address = LocationUtils.getAddressFromLocation(mLocation);

                }
            });
    }

    public static void stopLocationUpdates() {
        if (getGoogleApiClient().isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), new LocationListener() {
                @Override
                public void onLocationChanged(Location mLocation) {
                    DebugUtils.logDebug(TAG, "stopLocationUpdates() onLocationChanged: " + mLocation.toString());
                }
            });
    }


    public static LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setNumUpdates(1);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }

    public static Location getCurrentLocation() {
        Location mCurrentLocation = null;
        if (getGoogleApiClient().isConnected())
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());

        return mCurrentLocation;
    }

    public static void setAppiumLocation(boolean bLunch) {
        if (bLunch)
            LocationUtils.mCurrentLocation = new LatLng(37.784741, -122.402802);
        else
            LocationUtils.mCurrentLocation = new LatLng(37.767780, -122.414818);

        Order.address = LocationUtils.getAddressFromLocation(LocationUtils.mCurrentLocation);

    }
}
