package com.bluecats.scratchingpost;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;


import com.bluecats.scratchingpost.databinding.ActivityBeaconsBinding;
import com.bluecats.sdk.BCSite;
import com.bluecats.sdk.BlueCatsSDK;

public class BeaconsActivity extends BaseActivity {
	private static final String TAG = "BeaconsActivity";

	private ActivityBeaconsBinding mBinding;
	private BCSite mSite;
	private WebView webView;
	private ImageView loadingImage;

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

		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				webView.loadUrl(url);
                return false;
			}
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){

                handler.proceed();

            }
		});

		WebSettings settings = webView.getSettings();
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setJavaScriptEnabled(true);

		settings.setAppCacheEnabled(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setDatabaseEnabled(false);
		settings.setDomStorageEnabled(true);
		settings.setGeolocationEnabled(false);
		settings.setSaveFormData(false);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);

		webView.loadUrl(mSite.getCachedBeacons().get(0).getEddystone().getURL());

		loadingImage = (ImageView) findViewById(R.id.imageView);



		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)
			{
				setTitle("Loading...");
				loadingImage.setVisibility(view.VISIBLE);
				setProgress(progress * 100);

				if(progress > 95) {
					setTitle(mSite.getCachedBeacons().get(0).getEddystone().getURL());
					loadingImage.setVisibility(view.INVISIBLE);
				}
			}
		});
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
		((AudioManager) getSystemService(
				Context.AUDIO_SERVICE)).requestAudioFocus(
				null,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

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