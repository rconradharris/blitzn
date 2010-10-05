package com.barleysoft.blitzn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.TextView;

public class SetTime extends Activity {
	private Spinner durationSpinner;
	private Spinner incrementSpinner;
	private CheckBox shakeCheckbox;
	private CheckBox flipCheckbox;
	private CheckBox soundCheckbox;

	int getIndexOfChoice(String[] choices, int value) {
		for (int i = 0; i < choices.length; i++) {
			int cur = Integer.parseInt(choices[i].split(" ")[0]);
			if (cur == value)
				return i;
		}
		return -1;
	}

	Spinner setupSpinner(int spinnerId, int choicesId, int value) {
		// value is value of the selection to pre-pop the spinner with

		Spinner spinner = (Spinner) findViewById(spinnerId);
		ArrayAdapter<?> arrayAdapter = ArrayAdapter.createFromResource(this,
				choicesId, android.R.layout.simple_spinner_item);
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(arrayAdapter);

		// Pre-pop the duration spinner
		String[] arrayChoices = getResources().getStringArray(choicesId);
		int spinnerSelection = getIndexOfChoice(arrayChoices, value);
		if (spinnerSelection != -1)
			spinner.setSelection(spinnerSelection);

		return spinner;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Blitzn", "SetTime Activity started");
		setContentView(R.layout.set_time);

		Bundle extras = getIntent().getExtras();
		durationSpinner = setupSpinner(R.id.durationSpinner,
				R.array.duration_choices, extras.getInt("durationMinutes"));
		incrementSpinner = setupSpinner(R.id.incrementSpinner,
				R.array.increment_choices, extras.getInt("incrementSeconds"));

		// Setup Shake To Reset Checkbox
		shakeCheckbox = (CheckBox) findViewById(R.id.shakeCheckbox);
		shakeCheckbox.setChecked(extras.getBoolean("shakeEnabled"));
		
		// Setup Flip To Pause Checkbox
		flipCheckbox = (CheckBox) findViewById(R.id.flipCheckbox);
		flipCheckbox.setChecked(extras.getBoolean("flipEnabled"));
		
		// Setup Sounds Checkbox
		soundCheckbox = (CheckBox) findViewById(R.id.soundCheckbox);
		soundCheckbox.setChecked(extras.getBoolean("soundEnabled"));

		setVersionLabel();
	}

	private void setVersionLabel() {
		TextView versionLabel = (TextView) findViewById(R.id.versionLabel);
		versionLabel.setText("v" + getVersionName());
	}

	public String getVersionName() {
		try {
			PackageInfo manager = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			return manager.versionName;
		} catch (NameNotFoundException e) {
			return "0.0";
		}
	}

	@Override
	public void onBackPressed() {
		Bundle extras = new Bundle();

		// Duration selection
		String minuteChoice = (String) durationSpinner.getSelectedItem();
		if (minuteChoice != null) {
			int minutes = Integer.parseInt(minuteChoice.split(" ")[0]);
			extras.putInt("durationMinutes", minutes);
		}

		// Increment selection
		String incrementChoice = (String) incrementSpinner.getSelectedItem();
		if (incrementChoice != null) {
			int increment = Integer.parseInt(incrementChoice.split(" ")[0]);
			extras.putInt("incrementSeconds", increment);
		}

		// Shake to Reset selection
		extras.putBoolean("shakeEnabled", shakeCheckbox.isChecked());

		// Flip to Pause selection
		extras.putBoolean("flipEnabled", flipCheckbox.isChecked());
		
		// Sound selection
		extras.putBoolean("soundEnabled", soundCheckbox.isChecked());

		Intent intent = new Intent();
		intent.putExtras(extras);
		setResult(RESULT_OK, intent);
		finish();
	}
}
