package com.bluecats.scratchingpost;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;

import com.bluecats.scratchingpost.adapters.BeaconSnifferAdapter;
import com.bluecats.scratchingpost.databinding.ActivityBeaconSnifferBinding;
import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BlueCatsSDK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeaconSnifferActivity extends BaseActivity
{
	private static final String TAG = "BeaconSnifferActivity";

	private final List<BCBeacon> mBeacons = Collections.synchronizedList( new ArrayList<BCBeacon>() );
	private final BeaconSnifferAdapter mBeaconsAdapter = new BeaconSnifferAdapter( mBeacons );
	private final BCBeaconManager mBeaconManager = new BCBeaconManager();

	private ActivityBeaconSnifferBinding mBinding;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mBinding = DataBindingUtil.setContentView( this, R.layout.activity_beacon_sniffer );

		setSupportActionBar( mBinding.toolbar );
        //mBeacons.get(0).getEddystone().getURL();
		mBinding.rcyBeaconsSniffer.setAdapter( mBeaconsAdapter );
		mBinding.rcyBeaconsSniffer.setLayoutManager( new LinearLayoutManager( this ) );

	}

	@Override
	protected void onResume()
	{
		super.onResume();

		Log.d( TAG, "onResume" );

		BlueCatsSDK.didEnterForeground();
		mBeaconManager.registerCallback( mBeaconManagerCallback );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		Log.d( TAG, "onPause" );

		BlueCatsSDK.didEnterBackground();
		mBeaconManager.unregisterCallback( mBeaconManagerCallback );
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		switch( item.getItemId() )
		{
			case android.R.id.home:
				onBackPressed();
				break;
		}

		return true;
	}

	private final BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback()
	{
		@Override
		public void didEnterBeacons( final List<BCBeacon> beacons )
		{
			Log.d( TAG, "didEnterBeacons: " + beacons.size() + " found" );

			for( final BCBeacon beacon : beacons )
			{
				if( !mBeacons.contains( beacon ) && beacon.isEddystone())
				{
					mBeacons.add( beacon );
					remove_dubs(beacons);

					runOnUiThread( new Runnable()
					{
						@Override
						public void run()
						{
							mBeaconsAdapter.notifyItemInserted( mBeacons.size() - 1 );
						}
					} );
				}

			}
		}

		@Override
		public void didRangeBeacons( final List<BCBeacon> beacons )
		{
			//A hack to add any beacons that have entered before this callback was created
			final List<BCBeacon> unfoundBeacons = new ArrayList<>();

			for( final BCBeacon beacon : beacons )
			{
				final int index = mBeacons.indexOf( beacon );
				if( index > -1 && beacon.isEddystone())
				{
					mBeacons.set( index, beacon );
				}
				else
				{
					//Beacon doesn't exist, add it
					unfoundBeacons.add( beacon );
				}
			}

			if( unfoundBeacons.size() > 0 )
			{
				didEnterBeacons( unfoundBeacons );
			}

			//Beacons are updated on every range, i.e. new RSSI, so update data set every time
			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					mBeaconsAdapter.notifyDataSetChanged();
				}
			} );

            remove_dubs(beacons);
			remove_distant(beacons);
		}

		@Override
		public void didExitBeacons( final List<BCBeacon> beacons )
		{
			for( final BCBeacon beacon : beacons )
			{
				if( mBeacons.contains( beacon ) )
				{
					final int index = mBeacons.indexOf( beacon );
					Log.d(TAG, "didExitBeacon: " + index + " " + beacon.getName() + " " + beacon.getSiteName());
					mBeacons.remove( index );

					runOnUiThread( new Runnable()
					{
						@Override
						public void run()
						{
							Log.d(TAG, "Notify removed " + index);
							mBeaconsAdapter.notifyItemRemoved( index );
						}
					} );
				}
			}
			remove_distant(beacons);
		}

		// Searches the list for duplicate beacons and removes them
		public void remove_dubs(final List<BCBeacon> beacons)
        {

            // make sure any duplicates are removed
            for(int i = 0; i < mBeacons.size(); i ++)
            {
                BCBeacon current_beacon = beacons.get(i);

                for(int j = 0; j < mBeacons.size(); j++)
                {
                    if (j!=i && current_beacon.getName().equals(mBeacons.get(j).getName()))
                    {
                        mBeacons.remove(j);

						final int indx = j;
						runOnUiThread( new Runnable()
						{
							@Override
							public void run()
							{
								mBeaconsAdapter.notifyItemRemoved( indx );
							}
						} );
                    }
                }
            }

			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					mBeaconsAdapter.notifyDataSetChanged();
				}
			} );
        }

        // removes every beacon
		public void remove_distant(final List<BCBeacon> beacons)
		{

			// make sure any duplicates are removed
			for(int i = 0; i < mBeacons.size(); i ++)
			{
				if (beacons.get(i).getProximity().getValue() < Constants.FURTHEST_DIST)
				{
					mBeacons.remove(i);
				}

			}

			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					mBeaconsAdapter.notifyDataSetChanged();
				}
			} );
		}
	};
}
