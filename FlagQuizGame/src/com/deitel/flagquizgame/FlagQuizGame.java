package com.deitel.flagquizgame;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class FlagQuizGame extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flag_quiz_game);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.flag_quiz_game, menu);
		return true;
	}

}
