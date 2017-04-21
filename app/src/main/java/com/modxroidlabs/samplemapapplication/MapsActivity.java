package com.modxroidlabs.samplemapapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;

import static com.modxroidlabs.samplemapapplication.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                              GoogleApiClient.ConnectionCallbacks,
                                                              GoogleApiClient.OnConnectionFailedListener,
                                                              LocationListener,
                                                              GoogleMap.OnMapClickListener,
                                                              GoogleMap.OnMapLongClickListener,
                                                              GoogleMap.OnMarkerClickListener
//GoogleMap.OnInfoWindowClickListener
{
    private GoogleMap mMap;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                                             .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //startLocationIntent();
        //setDirection();
        //getFirstLocation();
        //getStreetView();
        //getRestaurantsNearBy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //Adding Zoom Button and Compass
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        initListeners();

        //Initialize Google Play Services
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this,
                                                 android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                               .addConnectionCallbacks(this)
                               .addOnConnectionFailedListener(this)
                               .addApi(LocationServices.API)
                               .build();
        mGoogleApiClient.connect();
    }

    private void initListeners()
    {
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        //mMap.setOnInfoWindowClickListener( this );
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,
                                             android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if(mCurrLocationMarker != null)
        {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if(mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        drawOverlay(latLng, 5, 5);
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));

        options.icon(BitmapDescriptorFactory.defaultMarker());
        mMap.addMarker(options);
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));

        options.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(getResources(),
                                         R.mipmap.ic_launcher)));

        mMap.addMarker(options);
    }

    private String getAddressFromLatLng(LatLng latLng)
    {
        Geocoder geocoder = new Geocoder(this);

        String address = "";
        try
        {
            address = geocoder
                          .getFromLocation(latLng.latitude, latLng.longitude, 1)
                          .get(0)
                          .getAddressLine(0);
        }
        catch(IOException | IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }

        return address;
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        marker.showInfoWindow();
        return true;
    }

    private void drawCircle(LatLng location)
    {
        CircleOptions options = new CircleOptions();
        options.center(location);
        //Radius in meters
        options.radius(10);
        options.fillColor(getResources()
                              .getColor(android.R.color.holo_blue_dark));
        options.strokeColor(getResources()
                                .getColor(android.R.color.holo_orange_dark));
        options.strokeWidth(10);
        mMap.addCircle(options);
    }

    private void drawPolygon(LatLng startingLocation)
    {
        LatLng point2 = new LatLng(startingLocation.latitude + .001,
                                   startingLocation.longitude);
        LatLng point3 = new LatLng(startingLocation.latitude,
                                   startingLocation.longitude + .001);

        PolygonOptions options = new PolygonOptions();
        options.add(startingLocation, point2, point3);

        options.fillColor(getResources()
                              .getColor(android.R.color.holo_blue_dark));
        options.strokeColor(getResources()
                                .getColor(android.R.color.holo_orange_dark));
        options.strokeWidth(10);

        mMap.addPolygon(options);
    }

    private void drawOverlay(LatLng location, int width, int height)
    {
        GroundOverlayOptions options = new GroundOverlayOptions();
        options.position(location, width, height);

        options.image(BitmapDescriptorFactory
                          .fromBitmap(BitmapFactory
                                          .decodeResource(getResources(),
                                                          R.mipmap.ic_launcher)));
        mMap.addGroundOverlay(options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults)
    {
        switch(requestCode)
        {
        case MY_PERMISSIONS_REQUEST_LOCATION:
        {
            // If request is cancelled, the result arrays are empty.
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // permission was granted. Do the
                // contacts-related task you need to do.
                if(ContextCompat.checkSelfPermission(this,
                                                     android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    if(mGoogleApiClient == null)
                    {
                        buildGoogleApiClient();
                    }
                    mMap.setMyLocationEnabled(true);
                }
            }
            else
            {
                // Permission denied, Disable the functionality that depends on this permission.
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,
                                             android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Asking user if explanation is needed
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                   android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                                                  new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                                                  MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                                                  new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                                                  MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    //Demo Locations
    private void setDirection()
    {
        // Assign your origin and destination
        // These points are your markers coordinates
        LatLng origin = new LatLng(43.653908, -79.384293);
        LatLng dest = new LatLng(43.7732, -79.3357);

        // Getting URL for Google MAP direction
        Uri uri = GetDirectionURL.getDirectionsUrl(origin, dest);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
    }

    private void startLocationIntent()
    {
        //Intent to open the map
        Intent intent = new Intent(Intent.ACTION_VIEW, GetDirectionURL.getLocationMapByAddress("Lambton College, Toronto"));
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
    }

    private void getStreetView()
    {
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("google.streetview:cbll=46.414382,10.013988");

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        startActivity(mapIntent);

    }

    private void getFirstLocation()
    {
        //Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4192?q=" + Uri.encode("1st & Pike, Seattle"));
        Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void getRestaurantsNearBy()
    {
        // Search for restaurants in San Francisco
        Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194?q=restaurants");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

    }


}
