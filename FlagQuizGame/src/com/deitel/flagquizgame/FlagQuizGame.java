package com.deitel.flagquizgame;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class FlagQuizGame extends Activity {
	
	private static final String TAG = "FlagQuizGame Activity";
	
	private List<String> fileNameList;
	private List<String> quizCountriesList;
	private Map<String, Boolean> regionsMap;
	private String correctAnswer;
	private int totalGuesses;
	private int correctAnswers;
	private int guessRows;
	private Random random;
	private Handler handler;
	private Animation shakeAnimation;
	
	private TextView answerTextView;
	private TextView questionNumberTextView;
	private ImageView flagImageView;
	private TableLayout buttonTabeLayout;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		fileNameList = new ArrayList<String>();
		quizCountriesList = new ArrayList<String>();
		regionsMap = new HashMap<String, Boolean>();
		guessRows = 1;
		random = new Random();
		handler = new Handler();
		
		shakeAnimation 
			= AnimationUtils.loadAnimation(this, R.animator.incorrect_shake);
		shakeAnimation.setRepeatCount(3);
		
		String[] regionNames = 
				getResources().getStringArray(R.array.regionsList);
		
		for (String region : regionNames)
			regionsMap.put(region, true);
		
		questionNumberTextView = 
				(TextView) findViewById(R.id.questionNumberTextView);
		flagImageView = (ImageView) findViewById(R.id.flagImageView);
		buttonTabeLayout = 
				(TableLayout) findViewById(R.id.buttonTableLayout);
		answerTextView = (TextView) findViewById(R.id.answerTextView);
		
		questionNumberTextView.setText(
				getResources().getString(R.string.question) + " 1 " +
				getResources().getString(R.string.of) + " 10");
		
		resetQuiz();
	}

	private void resetQuiz() {
		
		AssetManager assets = getAssets();
		fileNameList.clear();
		
		try
		{
			Set<String> regions = regionsMap.keySet();
			
			for(String region : regions)
			{
				if(regionsMap.get(region))
				{
					String[] paths = assets.list(region);
					
					for(String path : paths)
						fileNameList.add(path.replace(".png", ""));
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Error loading iumage file names", e);
		}
		
		correctAnswers = 0;
		totalGuesses = 0;
		quizCountriesList.clear();
		
		int flagCounter = 1;
		int numberOfFlags = fileNameList.size();
		
		while(flagCounter <= 10){
			int randomIndex = random.nextInt(numberOfFlags);
			
			String fileName = fileNameList.get(randomIndex);
			
			if(!quizCountriesList.contains(fileName))
			{
				quizCountriesList.add(fileName);
				++flagCounter;
			}
		}
		
		loadNextFlag();
		
	}
	
	private void loadNextFlag() {
		
		String nextImageName = quizCountriesList.remove(0);
		correctAnswer = nextImageName;
		
		answerTextView.setText("");
		
		questionNumberTextView.setText(
			getResources().getString(R.string.question) + " " +
			(correctAnswers + 1) + " " + 
			getResources().getString(R.string.of) + " 10" );
		
		String region = 
				nextImageName.substring(0, nextImageName.indexOf('-'));
		
		AssetManager assets = getAssets();
		InputStream stream;
		
		try
		{
			stream = assets.open(region + "/" + nextImageName + ".png");
			
			Drawable flag = Drawable.createFromStream(stream, nextImageName);
			flagImageView.setImageDrawable(flag);
		} catch (IOException e) {
			Log.e(TAG, "Error loading " + nextImageName, e);
		}
		
		for(int row = 0; row < buttonTabeLayout.getChildCount(); ++row)
			((TableRow) buttonTabeLayout.getChildAt(row)).removeAllViews();
		
		Collections.shuffle(fileNameList);
		
		int correct = fileNameList.indexOf(correctAnswer);
		fileNameList.add(fileNameList.remove(correct));
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		
		for(int row = 0; row < guessRows; row++)
		{
			TableRow currentTableRow = getTableRow(row);
			
			for(int column = 0; column < 3; column++)
			{
				Button newGuessButton = 
						(Button) inflater.inflate(R.layout.guess_button, null);
				
				String fileName = fileNameList.get((row * 3) + column);
				newGuessButton.setText(getCountryName(fileName));
				
				newGuessButton.setOnClickListener(guessButtonListener);
				currentTableRow.addView(newGuessButton);
			}
		}
		
		int row = random.nextInt(guessRows);
		int column = random.nextInt(3);
		TableRow randomTableRow = getTableRow(row);
		String countryName = getCountryName(correctAnswer);
		((Button) randomTableRow.getChildAt(column)).setText(countryName);
	}
	
	private TableRow getTableRow(int row) {
		return (TableRow) buttonTabeLayout.getChildAt(row);
	}
	
	private String getCountryName(String name) {
		return name.substring(name.indexOf('-') + 1).replace('_', ' ');
	}
	
	private void submitGuess(Button guessButton) {
		
		String guess = guessButton.getText().toString();
		String answer = getCountryName(correctAnswer);
		++totalGuesses;
		
		if(guess.equals(answer))
		{
			++correctAnswers;
			
			answerTextView.setText(answer + "!");
			answerTextView.setTextColor(
					getResources().getColor(R.color.correct_answer));
			
			disableButtons();
			
			if(correctAnswers == 10)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.reset_quiz);
				
				builder.setMessage(String.format("%d %s, %.02f%% %s",
						totalGuesses, getResources().getString(R.string.guesses),
						(1000 / (double) totalGuesses),
						getResources().getString(R.string.correct)));
				
				builder.setCancelable(false);
				
				builder.setPositiveButton(R.string.regions, 
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int id) {
								resetQuiz();
							}
						}
					);
				
				AlertDialog resetDialog = builder.create();
				resetDialog.show();
			}
			else
			{
				handler.postDelayed(
					new Runnable()
					{
						@Override
						public void run()
						{
							loadNextFlag();
						}
					}, 1000);
			}
		}
		else
		{
			flagImageView.startAnimation(shakeAnimation);
			
			answerTextView.setText(R.string.incorrect_answer);
			answerTextView.setTextColor(
					getResources().getColor(R.color.incorrect_answer));
			guessButton.setEnabled(false);
		}
	}
	
	private void disableButtons() {
		
		for(int row = 0; row < buttonTabeLayout.getChildCount(); ++row)
		{
			TableRow tableRow = (TableRow) buttonTabeLayout.getChildAt(row);
			for(int i = 0; i < tableRow.getChildCount(); ++i)
				tableRow.getChildAt(i).setEnabled(false);
		}
	}
	
	private final int CHOICES_MENU_ID = Menu.FIRST;
	private final int REGIONS_MENU_ID = Menu.FIRST + 1;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);
		menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);
		
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case CHOICES_MENU_ID:
				final String[] possibleChoices = 
					getResources().getStringArray(R.array.guessesList);
				
				AlertDialog.Builder choicesBuilder = 
					new AlertDialog.Builder(this);
				choicesBuilder.setTitle(R.string.choices);
				
				choicesBuilder.setItems(R.array.guessesList,
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int item) {
	
								guessRows = Integer.parseInt(
										possibleChoices[item].toString()) / 3;
								resetQuiz();
							}
						}
				);
				
				AlertDialog choicesDialog = choicesBuilder.create();
				choicesDialog.show();
				return true;
			
			case REGIONS_MENU_ID:
				
				final String[] regionNames = 
					regionsMap.keySet().toArray(new String[regionsMap.size()]);
				
				boolean[] regionsEnabled = new boolean[regionsMap.size()];
				for(int i = 0; i < regionsEnabled.length; ++i)
					regionsEnabled[i] = regionsMap.get(regionNames[i]);
				
				AlertDialog.Builder regionsBuilder = 
					new AlertDialog.Builder(this);
				regionsBuilder.setTitle(R.string.regions);
				
				String[] displayNames = new String[regionNames.length];
				for(int i = 0; i < regionNames.length; ++i)
					displayNames[i] = regionNames[i].replace('_', ' ');
				
				regionsBuilder.setMultiChoiceItems(
						displayNames, regionsEnabled, 
						new DialogInterface.OnMultiChoiceClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {

								regionsMap.put(
									regionNames[which].toString(), isChecked);
								
							}
						}
					);
				
				regionsBuilder.setPositiveButton(R.string.reset_quiz, 
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int button) {

								resetQuiz();
								
							}
						}
					);
				
				AlertDialog regionsDialog = regionsBuilder.create();
				regionsDialog.show();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private OnClickListener guessButtonListener = new OnClickListener() {
		
		public void onClick(View v)
		{
			submitGuess((Button) v);
		}
	};
	

}
