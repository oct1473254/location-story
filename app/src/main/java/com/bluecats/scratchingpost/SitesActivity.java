package com.bluecats.scratchingpost;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.bluecats.scratchingpost.adapters.BeaconSnifferAdapter;
import com.bluecats.scratchingpost.adapters.SitesAdapter;
import com.bluecats.scratchingpost.databinding.ActivitySitesBinding;
import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BCSite;
import com.bluecats.sdk.BlueCatsSDK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SitesActivity extends BaseActivity
{
	private static final String TAG = "SitesActivity";
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;

	private final List<BCSite> mSitesInside = Collections.synchronizedList( new ArrayList<BCSite>() );
	private final List<BCSite> mSitesNearby = Collections.synchronizedList( new ArrayList<BCSite>() );
	private final SitesAdapter mAdapterSitesInside = new SitesAdapter( mSitesInside );
	private final SitesAdapter mAdapterSitesNearby = new SitesAdapter( mSitesNearby );
    private final List<BCBeacon> mBeacons = Collections.synchronizedList( new ArrayList<BCBeacon>() );
    private final BeaconSnifferAdapter mBeaconsAdapter = new BeaconSnifferAdapter( mBeacons );
	private final BCBeaconManager mBeaconManager = new BCBeaconManager();

	private ActivitySitesBinding mBinding;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mBinding = DataBindingUtil.setContentView( this, R.layout.activity_sites );

		setSupportActionBar( mBinding.toolbar );

		mBinding.rcySitesInside.setAdapter( mAdapterSitesInside );
		mBinding.rcySitesInside.setLayoutManager( new LinearLayoutManager( this ) );

		mBinding.rcySitesNearby.setAdapter( mAdapterSitesNearby );
		mBinding.rcySitesNearby.setLayoutManager( new LinearLayoutManager( this ) );

		BlueCatsSDK.startPurringWithAppToken( getApplicationContext(), Constants.BLUECATS_APP_TOKEN );
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		BlueCatsSDK.didEnterForeground();

		mBeaconManager.registerCallback( mBeaconManagerCallback );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		BlueCatsSDK.didEnterBackground();

		mBeaconManager.unregisterCallback( mBeaconManagerCallback );
	}

	private final BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback()
	{
		@Override
		public void didEnterSite( final BCSite site )
		{
			if( mSitesInside.contains( site ) )
			{
				//Update site in list
				mSitesInside.set( mSitesInside.indexOf( site ), site );
			}
			else
				if( !mSitesNearby.contains( site ) )
			{
				mSitesNearby.add( site );
			}

			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					mAdapterSitesInside.notifyDataSetChanged();
					mAdapterSitesNearby.notifyDataSetChanged();
				}
			} );
		}

		@Override
		public void didExitSite( final BCSite site )
		{
			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					if( mSitesInside.remove( site ) )
					{
						mAdapterSitesInside.notifyDataSetChanged();
					}

					if( mSitesNearby.add( site ) )
					{
						mAdapterSitesNearby.notifyDataSetChanged();
						mSitesNearby.remove(site);
					}
				}
			} );
		}
	};
}
