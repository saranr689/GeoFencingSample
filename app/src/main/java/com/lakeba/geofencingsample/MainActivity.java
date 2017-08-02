package com.lakeba.geofencingsample;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.RemoteException;
import android.provider.SyncStateContract;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_PLAY_SERVICE_RESOLUTION = 1237;
    private static final String DLOCATION = "D_LOCATION";
    private static final int REQUEST_CODE_INTENT_LOCATION_SETTINGS = 257;
    private static final int REQUEST_CODE_PERMISSION_FINE_LOCATION = 250;

    private PendingIntent mGeofencePendingIntent;
    Button startGeoFence;
    private GoogleApiClient mGoogleApiClient;
    private boolean isGPSEnabled;
    private LocationManager locationManager;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startGeoFence = (Button) findViewById(R.id.startGeoFence);
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        switch (errorCode) {
            case ConnectionResult.SUCCESS:
                setupLocationManager();
//                setupFCM();
                break;
            default:
                boolean userResolvableError = googleApiAvailability.isUserResolvableError(errorCode);

                if (userResolvableError) {
                    Dialog errorDialog = googleApiAvailability.getErrorDialog(this, errorCode, REQUEST_CODE_PLAY_SERVICE_RESOLUTION);
                    errorDialog.show();
                } else {
                    Toast.makeText(MainActivity.this, googleApiAvailability.getErrorString(errorCode), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
        startGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(MainActivity.this);
            }
        });


    }

    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            //no gps and no network
            Toast.makeText(this, "NoGpsConnection", Toast.LENGTH_SHORT).show();
            return;
        } else {
            //gps enabled.
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            } else {
                buildGoogleApiClient();

                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d(DLOCATION, "buildGoogleApiClient");

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            Log.d(DLOCATION, "Not connected buildGoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        } else {
            //Already connected
            Log.d(DLOCATION, "Alerdy connected buildGoogleApiClient");
            getLocation();

        }
    }

    private void getLocation() {
        Log.d(DLOCATION, "getLocation");
        try {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
            } else {
                if (mGoogleApiClient != null) {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Log.d(DLOCATION, "getLocation found" + mLastLocation.getLatitude());

                } else {
                    Log.d(DLOCATION, "getLocation googleApiClient not found");
                    buildGoogleApiClient();

                    if (mGoogleApiClient != null) {
                        mGoogleApiClient.connect();
                    }
                }
            }
        } catch (Exception ex) {
            return;
        }


    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_FINE_LOCATION);


        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_FINE_LOCATION);

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {


        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_FINE_LOCATION);
        }
    }

    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER| GeofencingRequest.INITIAL_TRIGGER_EXIT);

        builder.addGeofence(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("101")
                .setCircularRegion(11.041701, 77.044659,5000f)
                .setExpirationDuration(20000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(MainActivity.this);

        Log.d(DLOCATION, "onConnected: ");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_INTENT_LOCATION_SETTINGS) {
            Log.d("On_ACTIVITY_RES", "LOCATION_SER");
            getLocation();

        } else if (requestCode == REQUEST_CODE_PLAY_SERVICE_RESOLUTION && resultCode == RESULT_OK) {
            Log.d("On_ACTIVITY_RES", "FCM");
            setupLocationManager();
        } else {
            Toast.makeText(MainActivity.this, "Google play store not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
