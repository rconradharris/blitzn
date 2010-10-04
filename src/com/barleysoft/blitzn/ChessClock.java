package com.barleysoft.blitzn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

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


	private long player1TimeLeft = 0L;
	private long player2TimeLeft = 0L;

	private long player1IncrementLeft = 0L;
	private long player2IncrementLeft = 0L;

	private ClockView player1Clock;
	private ClockView player2Clock;

	private Handler handler = new Handler();

	// Configurations
	private long duration = 5 * 1 * 1000L;
	private long increment = 0L;
	private boolean shakeEnabled = true;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Go full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		// Setup Player 1
		player1Clock = (ClockView) findViewById(R.id.player1Clock);
		player1Clock.setIsFlipped(true);
		OnClickListener player1ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER1);
			}
		};
		player1Clock
				.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);

		// Setup Player 2
		player2Clock = (ClockView) findViewById(R.id.player2Clock);
		OnClickListener player2ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER2);
			}
		};
		player2Clock
				.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);
		
		// Initialize UI
		installShakeListener();
		resetClock();
	}

	void installShakeListener() {
		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// TODO(sirp): add configuration setting for shake
		ShakeListener shakeListener = new ShakeListener(this);
		shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				if (shakeEnabled) {
					vibe.vibrate(100);
					resetClock();
				}
			}
		});
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

			// Activate Player 1
			player1Clock.setClickable(true);
			player1Clock.setBackgroundColor(Color.BLACK);

			// Deactivate Player 2
			player2Clock.setBackgroundColor(Color.TRANSPARENT);
			player2Clock.setClickable(false);

			clockState = PLAYER1_RUNNING;
		} else {
			// TODO(sirp): theme-up colors
			player2IncrementLeft = increment;

			// Activate Player 2
			player2Clock.setClickable(true);
			player2Clock.setBackgroundColor(Color.BLACK);

			// Deactivate Player 1
			player1Clock.setBackgroundColor(Color.TRANSPARENT);
			player1Clock.setClickable(false);

			clockState = PLAYER2_RUNNING;
		}
	}

	void resetClock() {
		handler.removeCallbacks(updateTimeTask);

		player1TimeLeft = duration;
		player2TimeLeft = duration;

		player1IncrementLeft = increment;
		player2IncrementLeft = increment;

		player1Clock.setBackgroundColor(Color.BLACK);
		player1Clock.setClickable(true);

		player2Clock.setBackgroundColor(Color.BLACK);
		player2Clock.setClickable(true);

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
				duration = extras.getInt("durationMinutes") * 60 * 1000;
				increment = extras.getInt("incrementSeconds") * 1000;
				shakeEnabled = extras.getBoolean("shakeEnabled");
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
		
		setTimeIntent.putExtra("durationMinutes", (int) duration / 60 / 1000);
		setTimeIntent.putExtra("incrementSeconds", (int) increment / 1000);
		setTimeIntent.putExtra("shakeEnabled", shakeEnabled);
		
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