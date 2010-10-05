package com.barleysoft.blitzn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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

	// Preference Key-Value Store
	public static final String PREFS_NAME = "BlitznPrefs";

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

	private ClockButton player1Clock;
	private ClockButton player2Clock;

	private Handler handler = new Handler();
	private MediaPlayer clicker;

	// Configurations
	private long duration = 5 * 1 * 1000L;
	private long increment = 0L;
	private boolean shakeEnabled = true;
	private boolean soundEnabled = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Go full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		// Setup Player 1
		player1Clock = (ClockButton) findViewById(R.id.player1Clock);
		player1Clock.setIsFlipped(true);
		OnClickListener player1ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER1);
			}
		};
		player1Clock
				.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);

		// Setup Player 2
		player2Clock = (ClockButton) findViewById(R.id.player2Clock);
		OnClickListener player2ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER2);
			}
		};
		player2Clock
				.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);

		// Initialize everything
		initializeSound();
		restorePreferences();
		installShakeListener();
		resetClock();
	}

	// NOTE(sirp): leaving this out (a bit cargo-cult-ish at the moment
	// @Override
	// public void onSaveInstanceState(Bundle savedInstanceState) {
	// // Save UI state changes to the savedInstanceState.
	// // This bundle will be passed to onCreate if the process is
	// // killed and restarted.
	// savedInstanceState.putInt("clockState", clockState);
	// savedInstanceState.putLong("player1TimeLeft", player1TimeLeft);
	// savedInstanceState.putLong("player2TimeLeft", player2TimeLeft);
	// savedInstanceState
	// .putLong("player1IncrementLeft", player1IncrementLeft);
	// savedInstanceState
	// .putLong("player2IncrementLeft", player2IncrementLeft);
	// super.onSaveInstanceState(savedInstanceState);
	// }
	//
	// @Override
	// public void onRestoreInstanceState(Bundle savedInstanceState) {
	// super.onRestoreInstanceState(savedInstanceState);
	// // Restore UI state from the savedInstanceState.
	// // This bundle has also been passed to onCreate.
	// clockState = savedInstanceState.getInt("clockState");
	// player1TimeLeft = savedInstanceState.getLong("player1TimeLeft");
	// player2TimeLeft = savedInstanceState.getLong("player2TimeLeft");
	// player1IncrementLeft =
	// savedInstanceState.getLong("player1IncrementLeft");
	// player2IncrementLeft =
	// savedInstanceState.getLong("player2IncrementLeft");
	// }

	void initializeSound() {
		clicker = MediaPlayer.create(this, R.raw.click1);
		// clicker will be null if sound is not supported on device
		if (clicker != null)
			clicker.setVolume(1.0f, 1.0f);
	}

	void playClick() {
		if ((clicker != null) && soundEnabled)
			clicker.start();
	}

	void restorePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		duration = settings.getLong("duration", 5 * 60 * 1000L);
		increment = settings.getLong("increment", 0L);
		shakeEnabled = settings.getBoolean("shakeEnabled", true);
		soundEnabled = settings.getBoolean("soundEnabled", true);
	}

	void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("duration", duration);
		editor.putLong("increment", increment);
		editor.putBoolean("shakeEnabled", shakeEnabled);
		editor.putBoolean("soundEnabled", soundEnabled);
		editor.commit();
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
			player1IncrementLeft = increment;
			player1Clock.setIsActivated(true);
			player2Clock.setIsActivated(false);
			playClick();
			clockState = PLAYER1_RUNNING;
		} else {
			player2IncrementLeft = increment;
			player2Clock.setIsActivated(true);
			player1Clock.setIsActivated(false);
			playClick();
			clockState = PLAYER2_RUNNING;
		}
	}

	void resetClock() {
		handler.removeCallbacks(updateTimeTask);

		player1TimeLeft = duration;
		player1IncrementLeft = increment;
		player1Clock.setIsActivated(true);

		player2TimeLeft = duration;
		player2IncrementLeft = increment;
		player2Clock.setIsActivated(true);

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

	private void updateClockForPlayer(ClockButton clockView, long timeLeft) {
		int seconds = (int) timeLeft / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;
		String clockText = String.format("%02d:%02d", minutes, seconds);
		clockView.setText(clockText);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// Back button on SetTime sub-Activity is overridden to bundle the
		// results and RETURN_OK
		Bundle extras = intent.getExtras();
		long old_duration = duration;
		duration = extras.getInt("durationMinutes") * 60 * 1000;

		long old_increment = increment;
		increment = extras.getInt("incrementSeconds") * 1000;

		shakeEnabled = extras.getBoolean("shakeEnabled");
		soundEnabled = extras.getBoolean("soundEnabled");

		// Only reset the clock if we changed something related to time-keeping
		if ((old_duration != duration) || (old_increment != increment))
			resetClock();
	}

	private void setTime() {
		Intent setTimeIntent = new Intent(this, SetTime.class);
		Log.i("Blitzn", "Launching SetTime Intent");

		setTimeIntent.putExtra("durationMinutes", (int) duration / 60 / 1000);
		setTimeIntent.putExtra("incrementSeconds", (int) increment / 1000);
		setTimeIntent.putExtra("shakeEnabled", shakeEnabled);
		setTimeIntent.putExtra("soundEnabled", soundEnabled);

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

	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();
	}

	private void showExitDialog() {
		// Ask the user if they want to quit
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.quit)
				.setMessage(R.string.leave_in_progress)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								// Stop the activity
								ChessClock.this.finish();
							}
						}).setNegativeButton(R.string.no, null).show();
	}

	public boolean isGameInProgress() {
		switch (clockState) {
		case NOSTATE:
		case READY:
		case STOPPED:
			return false;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (isGameInProgress())
			showExitDialog();
		else
			super.onBackPressed();
	}
}