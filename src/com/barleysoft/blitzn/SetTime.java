package com.barleysoft.blitzn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View.OnClickListener;

public class SetTime extends Activity {
	private Spinner minuteSpinner;
	private Spinner incrementSpinner;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Blitzn", "SetTime Activity started");
        setContentView(R.layout.set_time);
        
        minuteSpinner = (Spinner) findViewById(R.id.minuteSpinner);
        ArrayAdapter minuteAdapter = ArrayAdapter.createFromResource(
                this, R.array.minute_choices, android.R.layout.simple_spinner_item);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);
 
        incrementSpinner = (Spinner) findViewById(R.id.incrementSpinner);
        ArrayAdapter incrementAdapter = ArrayAdapter.createFromResource(
                this, R.array.increment_choices, android.R.layout.simple_spinner_item);
        incrementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incrementSpinner.setAdapter(incrementAdapter);
        
        Button setTimeOK = (Button) findViewById(R.id.setTimeOK);

        setTimeOK.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Bundle extras = new Bundle();
        		String minuteChoice = (String) minuteSpinner.getSelectedItem();
        		if (minuteChoice != null) {
        			int minutes = Integer.parseInt(minuteChoice.split(" ")[0]);
        			extras.putInt("minutes", minutes);
        		}

        		String incrementChoice = (String) incrementSpinner.getSelectedItem();
        		if (incrementChoice != null) {
        			int increment = Integer.parseInt(incrementChoice.split(" ")[0]);
        			extras.putInt("increment", increment);
        		}
        		
        		Intent intent = new Intent();
        		intent.putExtras(extras);
        		setResult(RESULT_OK, intent);
        		finish();
        	}
        });
    }
}
