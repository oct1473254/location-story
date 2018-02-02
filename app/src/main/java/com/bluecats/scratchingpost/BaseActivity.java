package com.bluecats.scratchingpost;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.util.Log;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BCSite;
import com.bluecats.sdk.BlueCatsSDK;

import java.net.URL;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    protected ApplicationPermissions mPermissions;
    private static final String TAG = "BaseActivity";
    private BCBeacon closestBCBeacon, lastClosestBCBeacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_site);

        mPermissions = new ApplicationPermissions(BaseActivity.this);
        mPermissions.verifyPermissions();

        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), Constants.BLUECATS_APP_TOKEN);

        final BCBeaconManager beaconManager = new BCBeaconManager();
        beaconManager.registerCallback(mBeaconManagerCallback);

    }

    @Override
    protected void onResume() {
        super.onResume();

        BlueCatsSDK.didEnterForeground();
    }

    @Override
    protected void onPause() {
        super.onPause();

        BlueCatsSDK.didEnterBackground();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (mPermissions != null) {
                mPermissions.verifyPermissions();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mPermissions != null) {
            mPermissions.onRequestPermissionResult(requestCode, permissions, grantResults);
        }
    }

    private BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback() {
        @Override
        public void didRangeBeacons(final List<BCBeacon> beacons) {
            Log.d(TAG, "didRangeBeacons: " + beacons.size());
            if( !beacons.isEmpty() ) {
                lastClosestBCBeacon = closestBCBeacon;
                closestBCBeacon = getClosestBeacon(beacons);
                Log.d( TAG, "Closest Beacon: " + closestBCBeacon.getName() );

                if ( lastClosestBCBeacon != null && !lastClosestBCBeacon.equals(closestBCBeacon) && closestBCBeacon.isEddystone()) {
                    notifyUser();
                }
            }
        }

        @Override
        public void didRangeEddystoneBeacons(final List<BCBeacon> eddystoneBeacons) {
            Log.d(TAG, "EddyStone Beacons: " + eddystoneBeacons.size());
        }
        public void didEnterSite( final BCSite site ) {
            Log.d(TAG, "Entered Site: " + site);
        }
        @Override
        public void didDiscoverEddystoneURL(final URL eddystoneUrl) {
            Log.d(TAG, "EddyStone URL: " + eddystoneUrl.toString());
        }
        public void didDetermineState( final BCSite.BCSiteState state, final BCSite forSite ) {
        }

        @Override
        public void didExitBeacons( final List<BCBeacon> beacons ) {
            Log.d(TAG, "Exit Beacons: " + beacons);
        }
        @Override
        public void didExitSite( final BCSite site ) {
            Log.d(TAG, "Exit site: " + site);
        }
    };

    private BCBeacon getClosestBeacon(List<BCBeacon> beacons) {
        int i = 0;
        BCBeacon closestBCBeacon = beacons.get(0);
        while (beacons.size() > i) {
            if (beacons.get(i).getProximity().getValue() < closestBCBeacon.getProximity().getValue()) {
                closestBCBeacon = beacons.get(i);
            }
            i++;
        }
        return closestBCBeacon;
    }

    private void notifyUser() {
        Intent intent = new Intent(BaseActivity.this, SitesActivity.class);

        final PendingIntent pIntent = PendingIntent.getActivity(BaseActivity.this, 0, intent, 0);

        Notification notification = new Notification.Builder(BaseActivity.this)
                .setTicker("TickerTitle")
                .setContentTitle("There is a story nearby!")
                .setContentText("Tap to view")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent.getActivity(this, 0, intent, 0))
                .getNotification();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification);
    }
}