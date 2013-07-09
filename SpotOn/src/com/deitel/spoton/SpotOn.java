package com.deitel.spoton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class SpotOn extends Activity {

	private SpotOnView view;
	
	@Override
	public void OnCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		RelativeLayout layout = 
				(RelativeLayout) findViewById(R.id.relativeLayout);
		view = new SpotOnView(this, getPreferences(Context.MODE_PRIVATE),
				layout);
		layout.addView(view, 0);
	}
	

	@Override
	public void onPause() {
		
		super.onPause();
		view.pause();
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
		view.resume(this);
	}
	
}
