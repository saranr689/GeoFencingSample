package com.lakeba.geofencingsample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by lakeba01 on 7/12/16.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final String D_INTENTSEVICES ="D_intent_servics" ;



    public   GeofenceTransitionsIntentService()
    {
        super(GeofenceTransitionsIntentService.class.getSimpleName());

    }
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.e(D_INTENTSEVICES, "onHandleIntent");
        if (geofencingEvent.hasError()) {

            Log.e(D_INTENTSEVICES, "hasError");
            Toast.makeText(this, "GEOFENCETRIGRED_ERROR", Toast.LENGTH_LONG).show();
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition,triggeringGeofences);

            // Send notification and log the transition details.
            sendNotification( geofenceTransition,triggeringGeofences);
//            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(D_INTENTSEVICES, "ERROR");
            Toast.makeText(this, "GEOFENCETRIGRED_ERROR", Toast.LENGTH_LONG).show();
        }
    }

    private void sendNotification(int geofenceTransition, List triggeringGeofences) {

        Log.d(D_INTENTSEVICES,geofenceTransition+triggeringGeofences.size()+"inSendNotification");
        Toast.makeText(this, "GEOFENCETRIGRED", Toast.LENGTH_LONG).show();

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle("GEOFENCETEST");
        Notification notification = notificationBuilder.build();
        notificationManager.notify(0, notification);


    }


}