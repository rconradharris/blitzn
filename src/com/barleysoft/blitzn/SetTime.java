package com.barleysoft.blitzn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.view.View.OnClickListener;

public class SetTime extends Activity {
	private Spinner durationSpinner;
	private Spinner incrementSpinner;
	private CheckBox shakeCheckbox;

	int getIndexOfChoice(String[] choices, int value) {
		for (int i = 0; i < choices.length; i++) {
			int cur = Integer.parseInt(choices[i].split(" ")[0]);
			if (cur == value)
				return i;
		}
		return -1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Blitzn", "SetTime Activity started");
		setContentView(R.layout.set_time);

		Bundle extras = getIntent().getExtras();

		// Setup Duration Spinner
		durationSpinner = (Spinner) findViewById(R.id.durationSpinner);
		ArrayAdapter<?> durationAdapter = ArrayAdapter.createFromResource(this,
				R.array.duration_choices, android.R.layout.simple_spinner_item);
		durationAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(durationAdapter);

		// Pre-pop the duration spinner
		String[] durationChoices = getResources().getStringArray(
				R.array.duration_choices);
		int durationSelection = getIndexOfChoice(durationChoices,
				extras.getInt("durationMinutes"));
		if (durationSelection != -1)
			durationSpinner.setSelection(durationSelection);

		// Setup Increment Spinner
		incrementSpinner = (Spinner) findViewById(R.id.incrementSpinner);
		ArrayAdapter<?> incrementAdapter = ArrayAdapter.createFromResource(
				this, R.array.increment_choices,
				android.R.layout.simple_spinner_item);
		incrementAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		incrementSpinner.setAdapter(incrementAdapter);

		// Pre-pop the increment spinner
		String[] incrementChoices = getResources().getStringArray(
				R.array.increment_choices);
		int incrementSelection = getIndexOfChoice(incrementChoices,
				extras.getInt("incrementSeconds"));
		if (incrementSelection != -1)
			incrementSpinner.setSelection(incrementSelection);

		// Setup Shake To Reset Checkbox
		shakeCheckbox = (CheckBox) findViewById(R.id.shakeCheckbox);
		shakeCheckbox.setChecked(extras.getBoolean("shakeEnabled"));

		// Setup SetTime Button
		Button setTimeOK = (Button) findViewById(R.id.setTimeOK);

		setTimeOK.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle extras = new Bundle();

				// Duration selection
				String minuteChoice = (String) durationSpinner
						.getSelectedItem();
				if (minuteChoice != null) {
					int minutes = Integer.parseInt(minuteChoice.split(" ")[0]);
					extras.putInt("durationMinutes", minutes);
				}

				// Increment selection
				String incrementChoice = (String) incrementSpinner
						.getSelectedItem();
				if (incrementChoice != null) {
					int increment = Integer.parseInt(incrementChoice.split(" ")[0]);
					extras.putInt("incrementSeconds", increment);
				}

				// Shake to Reset selection
				extras.putBoolean("shakeEnabled", shakeCheckbox.isChecked());

				Intent intent = new Intent();
				intent.putExtras(extras);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
}
