package com.barleysoft.blitzn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.TextView;

public class SetTime extends Activity {
	private Spinner durationSpinner;
	private Spinner delayTimeSpinner;
	private Spinner delayMethodSpinner;
	private CheckBox shakeCheckbox;
	private CheckBox flipCheckbox;
	private CheckBox soundCheckbox;

	int getIndexOfChoice(String[] choices, long value) {
		for (int i = 0; i < choices.length; i++) {
			long cur = Long.parseLong(choices[i].split(" ")[0]);
			if (cur == value)
				return i;
		}
		return -1;
	}

	Spinner setupSpinner(int spinnerId, int choicesId) {
		Spinner spinner = (Spinner) findViewById(spinnerId);
		ArrayAdapter<?> arrayAdapter = ArrayAdapter.createFromResource(this,
				choicesId, android.R.layout.simple_spinner_item);
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(arrayAdapter);
		return spinner;
	}

	void prePopSpinnerLong(Spinner spinner, int choicesId, long value) {
		// Pre-pop the duration spinner
		String[] arrayChoices = getResources().getStringArray(choicesId);
		int spinnerSelection = getIndexOfChoice(arrayChoices, value);
		if (spinnerSelection != -1)
			spinner.setSelection(spinnerSelection);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_time);

		Bundle extras = getIntent().getExtras();

		durationSpinner = setupSpinner(R.id.durationSpinner,
				R.array.duration_choices);
		prePopSpinnerLong(durationSpinner, R.array.duration_choices,
				extras.getLong("durationMinutes"));

		delayTimeSpinner = setupSpinner(R.id.delayTimeSpinner,
				R.array.delay_times);
		prePopSpinnerLong(delayTimeSpinner, R.array.delay_times,
				extras.getLong("delaySeconds"));

		delayMethodSpinner = setupSpinner(R.id.delayMethodSpinner,
				R.array.delay_methods);
		delayMethodSpinner.setSelection(extras.getInt("delayMethodSpinner"));

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
			long minutes = Long.parseLong(minuteChoice.split(" ")[0]);
			extras.putLong("durationMinutes", minutes);
		}

		// Delay Time selection
		String delayTimeChoice = (String) delayTimeSpinner.getSelectedItem();
		if (delayTimeChoice != null) {
			long delay = Long.parseLong(delayTimeChoice.split(" ")[0]);
			extras.putLong("delaySeconds", delay);
		}

		// Delay Method selection
		extras.putInt("delayMethod",
				delayMethodSpinner.getSelectedItemPosition());

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
