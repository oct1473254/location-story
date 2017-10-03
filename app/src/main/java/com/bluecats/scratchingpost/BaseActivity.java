package com.bluecats.scratchingpost;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.util.Log;

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

public class BaseActivity extends AppCompatActivity {
    protected ApplicationPermissions mPermissions;
    private static final String TAG = "BaseActivity";

    private BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback() {
        @Override
        public void didEnterSite( final BCSite site ) {
            Log.d( TAG , "Enter Site " + site.getName() );
        }

        @Override
        public void didExitSite( final BCSite site ) {
            Log.d( TAG , "Exit Site " + site.getName() );
        }

        @Override
        public void didDetermineState( final BCSite.BCSiteState state, final BCSite forSite ) {
            Log.d( TAG , "Determined Site " + state.name() );
        }

        @Override
        public void didEnterBeacons( final List<BCBeacon> beacons ) {
            Log.d( TAG , "didEnterBeacons: " + beacons.size() );
        }

        @Override
        public void didExitBeacons( final List<BCBeacon> beacons ) {
            Log.d( TAG , "Exit Beacons" + beacons.toString());
        }

        @Override
        public void didDetermineState( final BCBeacon.BCBeaconState state, final BCBeacon forBeacon ) {}

        @Override
        public void didRangeBeacons( final List<BCBeacon> beacons ) {
            Log.d( TAG , "didRangeBeacons: " + beacons.size() );
        }

        @Override
        public void didRangeBlueCatsBeacons( final List<BCBeacon> beacons ) {}

        @Override
        public void didRangeNewbornBeacons( final List<BCBeacon> newBornBeacons ) {}

        @Override
        public void didRangeIBeacons( final List<BCBeacon> iBeacons ) {}

        @Override
        public void didRangeEddystoneBeacons( final List<BCBeacon> eddystoneBeacons ) {
            Log.d( TAG , "EddyStone URL: "  + eddystoneBeacons.size());
        }

        @Override
        public void didDiscoverEddystoneURL( final URL eddystoneUrl ) {
            Log.d( TAG , "EddyStone URL: "  + eddystoneUrl.toString());
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.item_site );

        mPermissions = new ApplicationPermissions(BaseActivity.this);
        mPermissions.verifyPermissions();

        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), "6ff1e3ca-6f8d-401a-9d7a-c113fe054a09");

        final BCBeaconManager beaconManager = new BCBeaconManager();
        beaconManager.registerCallback( mBeaconManagerCallback );

        //final List<String> zoneIdentifierKeys = Arrays.asList( "ZONE_ID_KEY" );
        //final BCZoneMonitor zoneMonitor = new BCZoneMonitor( mZoneMonitorCallback, zoneIdentifierKeys );
        //zoneMonitor.startMonitoringZones();

    }

    //private final BCZoneMonitorCallback mZoneMonitorCallback = new BCZoneMonitorCallback() {}

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
}
