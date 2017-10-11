package com.bluecats.scratchingpost;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.*;
import android.util.Log;
import android.view.View;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BCSite;
import com.bluecats.sdk.BCZoneMonitor;
import com.bluecats.sdk.BCZoneMonitorCallback;
import com.bluecats.sdk.BlueCatsSDK;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static android.os.Build.VERSION_CODES.N;

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
        public void didEnterSite(final BCSite site) {
            Log.d(TAG, "Enter Site " + site.getName());
        }

        @Override
        public void didExitSite(final BCSite site) {
            Log.d(TAG, "Exit Site " + site.getName());
        }

        @Override
        public void didDetermineState(final BCSite.BCSiteState state, final BCSite forSite) {
            Log.d(TAG, "Determined Site " + state.name());
        }

        @Override
        public void didEnterBeacons(final List<BCBeacon> beacons) {
            Log.d(TAG, "didEnterBeacons: " + beacons.size());
        }

        @Override
        public void didExitBeacons(final List<BCBeacon> beacons) {
            Log.d(TAG, "Exit Beacons" + beacons.toString());
        }

        @Override
        public void didDetermineState(final BCBeacon.BCBeaconState state, final BCBeacon forBeacon) {
        }

        @Override
        public void didRangeBeacons(final List<BCBeacon> beacons) {
            Log.d(TAG, "didRangeBeacons: " + beacons.size());
            if( !beacons.isEmpty() ) {
                lastClosestBCBeacon = closestBCBeacon;
                closestBCBeacon = getClosestBeacon(beacons);
                Log.d( TAG, "Closest Beacon: " + closestBCBeacon.getName() );

                if ( lastClosestBCBeacon != null && !lastClosestBCBeacon.equals(closestBCBeacon) && closestBCBeacon.isEddystone()) {
                    //webDirect();
                    notifyUser();
                }
            }

        }

        @Override
        public void didRangeBlueCatsBeacons(final List<BCBeacon> beacons) {
        }

        @Override
        public void didRangeNewbornBeacons(final List<BCBeacon> newBornBeacons) {
        }

        @Override
        public void didRangeIBeacons(final List<BCBeacon> iBeacons) {
        }

        @Override
        public void didRangeEddystoneBeacons(final List<BCBeacon> eddystoneBeacons) {
            Log.d(TAG, "EddyStone Beacons: " + eddystoneBeacons.size());
        }

        @Override
        public void didDiscoverEddystoneURL(final URL eddystoneUrl) {
            Log.d(TAG, "EddyStone URL: " + eddystoneUrl.toString());

            webDirect(eddystoneUrl);
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

    private void webDirect(final URL eddystoneUrl) {
        Uri uri = Uri.parse(eddystoneUrl.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}