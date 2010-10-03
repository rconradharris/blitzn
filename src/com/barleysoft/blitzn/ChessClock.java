package com.barleysoft.blitzn;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ChessClock extends Activity {

	// Constants
	public static int PLAYER1 = 1;
	public static int PLAYER2 = 2;
	public static int CLOCK_RESOLUTION = 100; // ms

	// Activities
	public static final int ACTIVITY_SET_TIME = 0;
	
	// Menu Items
    public static final int MENU_RESET = Menu.FIRST;
    public static final int MENU_SET_TIME = Menu.FIRST + 1;

	// Clock State Machine
	public static final int NOSTATE = 0;
	public static final int READY = 1;
	public static final int PLAYER1_RUNNING = 2;
	public static final int PLAYER2_RUNNING = 3;
	public static final int PLAYER1_PAUSED = 4;
	public static final int PLAYER2_PAUSED = 5;
	public static final int STOPPED = 6;

	// Member variables
	private int clockState = NOSTATE;
	
	private long duration = 5 * 1 * 1000L;
	private long player1TimeLeft = 0L;
	private long player2TimeLeft = 0L;
	
	private long increment = 0L;
	private long player1IncrementLeft = 0L;
	private long player2IncrementLeft = 0L;
	
	private ClockView player1Clock;
	private ClockView player2Clock;

	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Setup Player 1
		player1Clock = (ClockView) findViewById(R.id.player1Clock);
		player1Clock.setIsFlipped(true);
		OnClickListener player1ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER1);
			}
		};
		player1Clock.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);
		
		// Setup Player 2
		player2Clock = (ClockView) findViewById(R.id.player2Clock);
		OnClickListener player2ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER2);
			}
		};
		player2Clock.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);
		resetClock();
	}

	void onPlayerClick(int which) {
		if (which == PLAYER1) {
			switch (clockState) {
			case READY:
				initiateClock(PLAYER1);
				break;
			case PLAYER1_RUNNING:
				toggleClock();
				break;
			case PLAYER1_PAUSED:
				pauseClock(); // unpause
				break;
			}
		} else {
			switch (clockState) {
			case READY:
				initiateClock(PLAYER2);
				break;
			case PLAYER2_RUNNING:
				toggleClock();
				break;
			case PLAYER2_PAUSED:
				pauseClock(); // unpause
				break;
			}
		}

	}

	void initiateClock(int which) {
		if (clockState != READY) {
			// throw new ClockStateException("already started");
		}

		activateClock(which);

		handler.removeCallbacks(updateTimeTask);
		handler.postDelayed(updateTimeTask, 100);
	}

	void activateClock(int which) {
		if (which == PLAYER1) {
			// TODO(sirp): theme-up colors
			player1IncrementLeft = increment;
			player1Clock.setClickable(true);
			player1Clock.setBackgroundColor(Color.RED);
			deactivateClock(PLAYER2, false);
			clockState = PLAYER1_RUNNING;
		} else {
			// TODO(sirp): theme-up colors
			player2IncrementLeft = increment;
			player2Clock.setClickable(true);
			player2Clock.setBackgroundColor(Color.RED);
			deactivateClock(PLAYER1, false);
			clockState = PLAYER2_RUNNING;
		}
	}

	void deactivateClock(int which, boolean clickable) {
		TextView clock = (which == PLAYER1) ? player1Clock : player2Clock;
		clock.setBackgroundColor(Color.BLACK);
		clock.setClickable(clickable);
	}

	void resetClock() {
		handler.removeCallbacks(updateTimeTask);
		player1TimeLeft = duration;
		player2TimeLeft = duration;
		player1IncrementLeft = increment;
		player2IncrementLeft = increment;
		deactivateClock(PLAYER1, true);
		deactivateClock(PLAYER2, true);
		updateClockDisplays();
		clockState = READY;
	}

	void toggleClock() {
		switch (clockState) {
		case PLAYER1_RUNNING:
			player1TimeLeft += player1IncrementLeft;
			activateClock(PLAYER2);
			break;
		case PLAYER2_RUNNING:
			player2TimeLeft += player2IncrementLeft;
			activateClock(PLAYER1);
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}

	void pauseClock() {
		switch (clockState) {
		case PLAYER1_RUNNING:
			handler.removeCallbacks(updateTimeTask);
			clockState = PLAYER1_PAUSED;
			break;
		case PLAYER2_RUNNING:
			handler.removeCallbacks(updateTimeTask);
			clockState = PLAYER2_PAUSED;
			break;
		case PLAYER1_PAUSED:
			handler.removeCallbacks(updateTimeTask);
			handler.postDelayed(updateTimeTask, 100);
			clockState = PLAYER1_RUNNING;
			break;
		case PLAYER2_PAUSED:
			handler.removeCallbacks(updateTimeTask);
			handler.postDelayed(updateTimeTask, 100);
			clockState = PLAYER2_RUNNING;
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}

	void stopClock() {
		handler.removeCallbacks(updateTimeTask);
		player1Clock.setClickable(false);
		player2Clock.setClickable(false);
		clockState = STOPPED;
	}

	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			// Check for either clock expiring
			if ((player1TimeLeft < CLOCK_RESOLUTION)
					|| (player2TimeLeft < CLOCK_RESOLUTION)) {
				stopClock();
				return;
			}

			switch (clockState) {
			case PLAYER1_RUNNING:
				player1TimeLeft -= CLOCK_RESOLUTION;
				player1IncrementLeft -= CLOCK_RESOLUTION;
				if (player1IncrementLeft < 0)
					player1IncrementLeft = 0L;
				break;
			case PLAYER2_RUNNING:
				player2TimeLeft -= CLOCK_RESOLUTION;
				player2IncrementLeft -= CLOCK_RESOLUTION;
				if (player2IncrementLeft < 0)
					player2IncrementLeft = 0L;
				break;
			}

			updateClockDisplays();

			// Reschedule the next tick
			long nextUpdate = SystemClock.uptimeMillis() + CLOCK_RESOLUTION;
			handler.postAtTime(this, nextUpdate);
		}
	};

	private void updateClockDisplays() {
		updateClockForPlayer(player1Clock, player1TimeLeft);
		updateClockForPlayer(player2Clock, player2TimeLeft);
	}

	private void updateClockForPlayer(ClockView clockView, long timeLeft) {
		int seconds = (int) timeLeft / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;
		String clockText = String.format("%02d:%02d", minutes, seconds);
		clockView.setText(clockText);
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Log.i("Blitzn", "onActivityResult called");

		// Back button pressed while in SetTime Intent
		if (intent == null)
			return;

		 Bundle extras = intent.getExtras();
		switch (requestCode) {
		case ACTIVITY_SET_TIME:
			switch (resultCode) {
			case RESULT_OK:
				int minutes = extras.getInt("minutes");
				duration = minutes * 60 * 1000;
				
				int incrementSeconds = extras.getInt("increment");
				increment = incrementSeconds * 1000;
				
				resetClock();
				break;
			case RESULT_CANCELED:
				break;
			}
			break;
		}
	}
	
    private void setTime() {
        Intent setTimeIntent = new Intent(this, SetTime.class);
        Log.i("Blitzn", "Launching SetTime Intent");
        startActivityForResult(setTimeIntent, ACTIVITY_SET_TIME);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_RESET, 0, "Reset");
        menu.add(0, MENU_SET_TIME, 0, "Set Time");
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RESET:
			resetClock();
			return true;
        case MENU_SET_TIME:
            setTime();
            return true;
		}
		return super.onOptionsItemSelected(item);
	}


	

}