package com.modxroidlabs.samplemapapplication;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by pritesh.patel on 2017-04-20
 */

public class GetDirectionURL
{
    //This technique is used to demo simple path
    //Use Google Direction API for advance and professional approach
    public static Uri getDirectionsUrl(LatLng origin, LatLng dest){


        // Origin of route
        String str_origin = "saddr=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "daddr=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Building the url to the web service
        String url = "http://maps.google.com/maps?" + parameters;
        Log.d(TAG, "getDirectionsUrl: " + url);

        return Uri.parse(url);
    }

    public static Uri getLocationMapByAddress(String addressString)
    {
        addressString = "1600 Amphitheatre Parkway, CA";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("geo")
                .path("0,0")
                .query(addressString);
        Uri addressUri = builder.build();

        return addressUri;

    }
}
