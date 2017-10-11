package com.bluecats.scratchingpost;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.bluecats.scratchingpost.adapters.BeaconsTabAdapter;
import com.bluecats.scratchingpost.databinding.ActivityBeaconsBinding;
import com.bluecats.scratchingpost.fragments.BeaconProximityFragment;
import com.bluecats.sdk.BCBeacon.BCProximity;
import com.bluecats.sdk.BCCategory;
import com.bluecats.sdk.BCEventFilter;
import com.bluecats.sdk.BCEventManager;
import com.bluecats.sdk.BCEventManagerCallback;
import com.bluecats.sdk.BCLocalNotification;
import com.bluecats.sdk.BCLocalNotificationManager;
import com.bluecats.sdk.BCSite;
import com.bluecats.sdk.BCTrigger;
import com.bluecats.sdk.BCTriggeredEvent;
import com.bluecats.sdk.BlueCatsSDK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BeaconsActivity extends BaseActivity {
	private static final String TAG = "BeaconsActivity";

	// each notification in your app will need a unique id
	private static final int NOTIFICATION_ID = 11;

	private ActivityBeaconsBinding mBinding;
	private BCSite mSite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_beacons);

		setSupportActionBar(mBinding.toolbar);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		final Intent intent = getIntent();
		mSite = intent.getParcelableExtra(BlueCatsSDK.EXTRA_SITE);
		setTitle(mSite.getName());

		final BeaconsTabAdapter tabAdapter = new BeaconsTabAdapter(getSupportFragmentManager(), Arrays.asList(
				BeaconProximityFragment.newInstance(mSite, BCProximity.BC_PROXIMITY_IMMEDIATE),
				BeaconProximityFragment.newInstance(mSite, BCProximity.BC_PROXIMITY_NEAR),
				BeaconProximityFragment.newInstance(mSite, BCProximity.BC_PROXIMITY_FAR),
				BeaconProximityFragment.newInstance(mSite, BCProximity.BC_PROXIMITY_UNKNOWN)
		));

		mBinding.viewPager.setAdapter(tabAdapter);
		mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
		mBinding.tabLayout.setTabTextColors(Color.argb(128, 255, 255, 255), Color.WHITE);

		final BCLocalNotification localNotification = new BCLocalNotification(NOTIFICATION_ID);
		// optional time to trigger the event after, eg 10 seconds from now
		//localNotification.setFireAfter(new Date(new Date().getTime() + (10 * 1000)));

		// add a category or several categories to trigger the notification
		final BCCategory category = new BCCategory();
		category.setName("CATEGORY_NAME");

		final List<BCCategory> categories = new ArrayList<>();
		categories.add(category);

		localNotification.setFireInCategories(categories);

		// can add an optional proximity to trigger event
		localNotification.setFireInProximity(BCProximity.BC_PROXIMITY_IMMEDIATE);

		// set alert title and content
		localNotification.setAlertContentTitle("There is a story Nearby!");
		localNotification.setAlertContentText("Click to view");

		// launch icon and ringtone are optional. will just default ringtone and app icon for defaults
		localNotification.setAlertSmallIcon(R.mipmap.ic_launcher);

		// this controls where the notification takes you.
		// can also contain a bundle or any extra info that you might want to unpack
		final Intent contentIntent = new Intent(BeaconsActivity.this, SitesActivity.class);
		localNotification.setContentIntent(contentIntent);

		BCLocalNotificationManager.getInstance().scheduleLocalNotification(localNotification);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG, "onResume");

		BlueCatsSDK.didEnterForeground();
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG, "onPause");

		BlueCatsSDK.didEnterBackground();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
		}

		return true;
	}

	private void createBasicTrigger() {
		//generate a trigger with a random identifier
		final BCTrigger trigger = new BCTrigger();

		//filter by sites
		trigger.addFilter(BCEventFilter.filterBySitesNamed(Arrays.asList(
				"my desk",
				"Long Table"
		)));

		trigger.addFilter(BCEventFilter.filterByClosestBeaconChanged());

		//filter to within 10cm
		//trigger.addFilter(BCEventFilter.filterByAccuracyRangeFrom(0.0, 0.1));

		//repeat indefinitely, or however many times you want
		trigger.setRepeatCount(Integer.MAX_VALUE);

		//add your trigger to the event manager
		BCEventManager.getInstance().monitorEventWithTrigger(trigger, mEventManagerCallback);
	}

	private final BCEventManagerCallback mEventManagerCallback = new BCEventManagerCallback() {
		@Override
		public void onTriggeredEvent(final BCTriggeredEvent bcTriggeredEvent) {

		}
	};
}