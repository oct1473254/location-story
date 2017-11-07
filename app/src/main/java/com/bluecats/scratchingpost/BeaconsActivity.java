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
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_beacons);

		setSupportActionBar(mBinding.toolbar);
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		final Intent intent = getIntent();
		mSite = intent.getParcelableExtra(BlueCatsSDK.EXTRA_SITE);
		setTitle(mSite.getCachedBeacons().get(0).getEddystone().getURL());

		webView = (WebView) findViewById(R.id.webView1);
		webView.setWebViewClient(new WebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(mSite.getCachedBeacons().get(0).getEddystone().getURL());
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
}